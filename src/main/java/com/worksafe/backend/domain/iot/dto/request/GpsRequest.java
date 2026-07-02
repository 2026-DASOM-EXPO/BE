package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GpsRequest(
        @NotNull Long workerId,
        Long equipmentId,
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double speed,
        LocalDateTime measuredAt
) {
}
