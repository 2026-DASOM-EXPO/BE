package com.worksafe.backend.domain.risk.dto.request;

import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RiskEventCreateRequest(
        Long workerId,
        @NotNull RiskSourceType sourceType,
        @NotNull RiskType riskType,
        @NotNull RiskLevel riskLevel,
        @NotBlank String description,
        Double latitude,
        Double longitude,
        LocalDateTime occurredAt,
        RiskStatus status
) {
    public RiskEventCreateRequest(
            Long workerId,
            RiskSourceType sourceType,
            RiskType riskType,
            RiskLevel riskLevel,
            String description,
            Double latitude,
            Double longitude,
            LocalDateTime occurredAt
    ) {
        this(workerId, sourceType, riskType, riskLevel, description, latitude, longitude, occurredAt, RiskStatus.OPEN);
    }
}
