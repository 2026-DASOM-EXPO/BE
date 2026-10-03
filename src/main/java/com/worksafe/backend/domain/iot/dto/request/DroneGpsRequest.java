package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

public record DroneGpsRequest(
        @NotNull Long droneId,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
