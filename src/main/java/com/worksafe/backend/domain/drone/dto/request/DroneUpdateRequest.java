package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.DroneStatus;
import jakarta.validation.constraints.Size;

public record DroneUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 100) String serialNumber,
        @Size(max = 100) String modelName,
        DroneStatus status,
        Integer batteryPercent,
        Double currentLatitude,
        Double currentLongitude,
        Integer maxFlightMinutes,
        Boolean payloadMounted
) {
}
