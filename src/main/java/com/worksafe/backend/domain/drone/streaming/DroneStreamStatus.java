package com.worksafe.backend.domain.drone.streaming;

import java.time.LocalDateTime;

public record DroneStreamStatus(
        String streamKey,
        boolean enabled,
        boolean running,
        boolean manifestReady,
        String playlistUrl,
        Long processId,
        String sourceType,
        LocalDateTime startedAt,
        String lastError
) {
}
