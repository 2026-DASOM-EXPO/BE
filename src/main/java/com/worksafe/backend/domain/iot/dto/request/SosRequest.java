package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SosRequest(
        @NotNull Long workerId,
        Double latitude,
        Double longitude,
        LocalDateTime measuredAt
) {
}
