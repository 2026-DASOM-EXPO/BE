package com.worksafe.backend.domain.drone.dto.response;

import com.worksafe.backend.domain.drone.enums.DroneStatus;

import java.time.LocalDateTime;

public record DroneResponse(
        Long id,
        String name,
        String serialNumber,
        String modelName,
        DroneStatus status,
        Integer batteryPercent,
        Double currentLatitude,
        Double currentLongitude,
        Integer maxFlightMinutes,
        boolean payloadMounted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
