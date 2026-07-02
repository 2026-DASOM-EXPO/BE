package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BiometricRequest(
        @NotNull Long workerId,
        Long equipmentId,
        Integer bpm,
        Double spo2,
        Double bodyTemperature,
        LocalDateTime measuredAt
) {
}
