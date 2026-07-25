package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SosRequest(
        @NotNull Long workerId,
        Long equipmentId,
        @NotNull @Min(0) @Max(1) Integer buttonValue,
        Double latitude,
        Double longitude,
        @NotBlank String message,
        LocalDateTime measuredAt
) {
}
