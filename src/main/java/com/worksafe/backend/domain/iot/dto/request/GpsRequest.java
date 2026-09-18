package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

public record GpsRequest(
        @NotNull Long workerId,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
