package com.worksafe.backend.domain.drone.dto.response;

import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import com.worksafe.backend.domain.drone.enums.StreamStatus;

import java.time.LocalDateTime;

public record DroneVideoResponse(
        Long id,
        DroneResponse drone,
        Long dispatchId,
        String title,
        String description,
        String streamUrl,
        VideoProtocol protocol,
        boolean active,
        StreamStatus streamStatus,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime lastFrameAt,
        LocalDateTime createdAt
) {
}
