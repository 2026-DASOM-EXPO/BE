package com.worksafe.backend.domain.equipment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BuzzerControlRequest(
        @NotNull Boolean enabled,
        @Size(max = 500) String reason
) {
}
