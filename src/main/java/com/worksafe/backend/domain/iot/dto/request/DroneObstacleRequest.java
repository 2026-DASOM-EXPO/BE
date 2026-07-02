package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DroneObstacleRequest(
        @NotNull Long droneId,
        Long dispatchId,
        Double lidarFrontLeft,
        Double lidarFrontRight,
        Double lidarBackLeft,
        Double lidarBackRight,
        Double lidarSideLeft,
        Double lidarSideRight,
        Double ultrasonicDistance,
        Boolean obstacleDetected,
        LocalDateTime measuredAt
) {
}
