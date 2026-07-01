package com.worksafe.backend.domain.risk.dto.response;

import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record RiskEventResponse(
        Long id,
        WorkerResponse worker,
        RiskSourceType sourceType,
        RiskType riskType,
        RiskLevel riskLevel,
        String description,
        Double latitude,
        Double longitude,
        RiskStatus status,
        LocalDateTime occurredAt,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
