package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SosRequest(
        @NotNull Long workerId,
        Long equipmentId,
        Double latitude,
        Double longitude,
        @NotBlank String message,
        LocalDateTime measuredAt
) {
}
