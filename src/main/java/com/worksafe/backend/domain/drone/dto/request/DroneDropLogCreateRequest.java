package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import jakarta.validation.constraints.NotNull;

public record DroneDropLogCreateRequest(
        @NotNull DropMethod dropMethod,
        Double targetLatitude,
        Double targetLongitude,
        Double actualDropLatitude,
        Double actualDropLongitude,
        Boolean obstacleDetected,
        Double lidarFrontLeft,
        Double lidarFrontRight,
        Double lidarBackLeft,
        Double lidarBackRight,
        Double lidarSideLeft,
        Double lidarSideRight,
        DropStatus dropStatus
) {
}
