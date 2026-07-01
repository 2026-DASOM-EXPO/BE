package com.worksafe.backend.domain.risk.dto.request;

import com.worksafe.backend.domain.risk.enums.RiskStatus;
import jakarta.validation.constraints.NotNull;

public record RiskEventStatusUpdateRequest(
        @NotNull RiskStatus status
) {
}
