package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.streaming.DroneStreamingProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/streams")
public class HlsStreamController {

    private static final Pattern SAFE_STREAM_KEY = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    private static final Pattern SAFE_HLS_FILE = Pattern.compile("(index\\.m3u8|segment_\\d{6,12}\\.ts)");
    private static final MediaType HLS_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.apple.mpegurl");
    private static final MediaType MPEG_TS_MEDIA_TYPE = MediaType.parseMediaType("video/mp2t");

    private final Path outputRoot;

    public HlsStreamController(DroneStreamingProperties properties) {
        this.outputRoot = Path.of(properties.outputPath()).toAbsolutePath().normalize();
    }

    @GetMapping("/{streamKey}/{fileName:.+}")
    public ResponseEntity<Resource> streamFile(
            @PathVariable String streamKey,
            @PathVariable String fileName
    ) throws MalformedURLException {
        if (!SAFE_STREAM_KEY.matcher(streamKey).matches() || !SAFE_HLS_FILE.matcher(fileName).matches()) {
            return ResponseEntity.notFound().build();
        }

        Path file = outputRoot.resolve(streamKey).resolve(fileName).normalize();
        if (!file.startsWith(outputRoot) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = fileName.endsWith(".m3u8") ? HLS_MEDIA_TYPE : MPEG_TS_MEDIA_TYPE;
        CacheControl cacheControl = fileName.endsWith(".m3u8")
                ? CacheControl.noStore()
                : CacheControl.maxAge(java.time.Duration.ofSeconds(5)).cachePublic();
        return ResponseEntity.ok()
                .contentType(contentType)
                .cacheControl(cacheControl)
                .body(new UrlResource(file.toUri()));
    }
}
