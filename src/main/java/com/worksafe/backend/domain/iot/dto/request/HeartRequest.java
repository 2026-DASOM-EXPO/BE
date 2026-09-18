package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HeartRequest(
        @NotNull Long workerId,
        @NotNull @Min(0) @Max(220) Integer bpm
) {
}
