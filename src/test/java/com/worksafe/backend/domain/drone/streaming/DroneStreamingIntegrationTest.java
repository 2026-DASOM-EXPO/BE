package com.worksafe.backend.domain.drone.streaming;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.drone.streaming.enabled=true",
        "app.drone.streaming.source-url-template=lavfi:testsrc=size=640x360:rate=30",
        "app.drone.streaming.output-directory=./build/test-hls",
        "app.drone.streaming.public-base-url=http://localhost:8080/streams",
        "app.drone.streaming.startup-timeout-seconds=20",
        "app.drone.streaming.width=640",
        "app.drone.streaming.height=360",
        "app.drone.streaming.frame-rate=30",
        "app.drone.streaming.segment-seconds=1",
        "app.drone.streaming.playlist-size=4"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DroneStreamingIntegrationTest {

    private static final String STREAM_KEY = "SIYI-E2E-TEST";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ffmpegInputIsExposedAsPublicHlsPlaylistAndSegment() throws Exception {
        Assumptions.assumeTrue(isFfmpegAvailable(), "로컬 또는 CI 환경에 FFmpeg가 없어 스트리밍 검증을 건너뜁니다.");

        try {
            mockMvc.perform(post("/api/drone-streams/{streamKey}/start", STREAM_KEY)
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.enabled").value(true))
                    .andExpect(jsonPath("$.data.running").value(true))
                    .andExpect(jsonPath("$.data.manifestReady").value(true))
                    .andExpect(jsonPath("$.data.sourceType").value("LAVFI"))
                    .andExpect(jsonPath("$.data.playlistUrl")
                            .value("http://localhost:8080/streams/" + STREAM_KEY + "/index.m3u8"));

            MvcResult playlistResult = mockMvc.perform(
                            get("/streams/{streamKey}/index.m3u8", STREAM_KEY))
                    .andExpect(status().isOk())
                    .andReturn();

            MediaType playlistContentType = playlistResult.getResponse().getContentType() == null
                    ? null
                    : MediaType.parseMediaType(playlistResult.getResponse().getContentType());
            assertThat(playlistContentType).isNotNull();
            assertThat(playlistContentType.isCompatibleWith(
                    MediaType.parseMediaType("application/vnd.apple.mpegurl"))).isTrue();

            String playlist = playlistResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(playlist).contains("#EXTM3U", "#EXT-X-INDEPENDENT-SEGMENTS");
            String firstSegment = Arrays.stream(playlist.split("\\R"))
                    .map(String::trim)
                    .filter(line -> line.matches("segment_\\d{6}\\.ts"))
                    .findFirst()
                    .orElseThrow();

            MvcResult segmentResult = mockMvc.perform(
                            get("/streams/{streamKey}/{fileName}", STREAM_KEY, firstSegment))
                    .andExpect(status().isOk())
                    .andReturn();

            MediaType segmentContentType = MediaType.parseMediaType(segmentResult.getResponse().getContentType());
            assertThat(segmentContentType.isCompatibleWith(MediaType.parseMediaType("video/mp2t"))).isTrue();
            assertThat(segmentResult.getResponse().getContentAsByteArray()).isNotEmpty();
        } finally {
            mockMvc.perform(delete("/api/drone-streams/{streamKey}", STREAM_KEY)
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    private boolean isFfmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception ignored) {
            return false;
        }
    }
}
