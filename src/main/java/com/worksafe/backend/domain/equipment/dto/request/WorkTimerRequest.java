package com.worksafe.backend.domain.equipment.dto.request;

import jakarta.validation.constraints.Size;

public record WorkTimerRequest(
        @Size(max = 500) String reason
) {
}
