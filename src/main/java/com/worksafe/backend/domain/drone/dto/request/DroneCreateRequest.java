package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.DroneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DroneCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String serialNumber,
        @NotBlank @Size(max = 100) String modelName,
        DroneStatus status,
        Integer batteryPercent,
        Double currentLatitude,
        Double currentLongitude,
        Integer maxFlightMinutes,
        @NotNull Boolean payloadMounted
) {
}
