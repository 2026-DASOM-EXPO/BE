package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

public record ImuRequest(
        @NotNull Long workerId,
        Long equipmentId,
        Double accelerationX,
        Double accelerationY,
        Double accelerationZ,
        Double gyroX,
        Double gyroY,
        Double gyroZ,
        Double tiltX,
        Double tiltY,
        Double tiltZ,
        Double impactAmount
) {
}
