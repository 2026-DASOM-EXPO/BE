package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HeartRequest(
        @NotNull Long workerId,
        Long equipmentId,
        @NotNull @Min(0) @Max(220) Integer bpm,
        LocalDateTime measuredAt
) {
}
