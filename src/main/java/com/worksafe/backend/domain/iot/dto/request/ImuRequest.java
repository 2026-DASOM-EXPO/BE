package com.worksafe.backend.domain.iot.dto.request;

import jakarta.validation.constraints.NotNull;

public record ImuRequest(
        @NotNull Long workerId,
        Double accelX,
        Double accelY,
        Double accelZ,
        Double gyroX,
        Double gyroY,
        Double gyroZ
) {
}
