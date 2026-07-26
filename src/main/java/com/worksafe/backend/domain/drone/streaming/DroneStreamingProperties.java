package com.worksafe.backend.domain.drone.streaming;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.drone.streaming")
public record DroneStreamingProperties(
        Boolean enabled,
        String ffmpegPath,
        String sourceUrlTemplate,
        String outputDirectory,
        String publicBaseUrl,
        Integer startupTimeoutSeconds,
        Integer width,
        Integer height,
        Integer frameRate,
        Integer segmentSeconds,
        Integer playlistSize
) {

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public String ffmpegExecutable() {
        return defaultIfBlank(ffmpegPath, "ffmpeg");
    }

    public String sourceTemplate() {
        return defaultIfBlank(sourceUrlTemplate, "rtsp://192.168.144.25:8554/main.264");
    }

    public String outputPath() {
        return defaultIfBlank(outputDirectory, "./build/hls");
    }

    public String publicUrl() {
        return stripTrailingSlash(defaultIfBlank(publicBaseUrl, "http://localhost:8080/streams"));
    }

    public int startupTimeout() {
        return positiveOrDefault(startupTimeoutSeconds, 15);
    }

    public int targetWidth() {
        return positiveOrDefault(width, 1280);
    }

    public int targetHeight() {
        return positiveOrDefault(height, 720);
    }

    public int targetFrameRate() {
        return positiveOrDefault(frameRate, 30);
    }

    public int hlsSegmentSeconds() {
        return positiveOrDefault(segmentSeconds, 1);
    }

    public int hlsPlaylistSize() {
        return positiveOrDefault(playlistSize, 6);
    }

    public String resolveSourceUrl(String streamKey) {
        return sourceTemplate().replace("{serialNumber}", streamKey);
    }

    public String playlistUrl(String streamKey) {
        return publicUrl() + "/" + streamKey + "/index.m3u8";
    }

    private static int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String stripTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
