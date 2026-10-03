package com.worksafe.backend.domain.drone.control.adapter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.drone.jetson")
public record JetsonCommandProperties(
        Boolean enabled,
        String baseUrl,
        String commandPath,
        Integer connectTimeoutMillis,
        Integer readTimeoutMillis,
        String authToken
) {

    public String requiredBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("app.drone.jetson.base-url is required when Jetson transport is enabled");
        }
        return baseUrl.trim().replaceAll("/+$", "");
    }

    public String resolvedCommandPath() {
        String path = commandPath == null || commandPath.isBlank()
                ? "/api/v1/drone/commands"
                : commandPath.trim();
        return path.startsWith("/") ? path : "/" + path;
    }

    public int resolvedConnectTimeoutMillis() {
        return positiveOrDefault(connectTimeoutMillis, 2_000);
    }

    public int resolvedReadTimeoutMillis() {
        return positiveOrDefault(readTimeoutMillis, 3_000);
    }

    public boolean hasAuthToken() {
        return authToken != null && !authToken.isBlank();
    }

    public String resolvedAuthToken() {
        return authToken == null ? "" : authToken.trim();
    }

    private static int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }
}
