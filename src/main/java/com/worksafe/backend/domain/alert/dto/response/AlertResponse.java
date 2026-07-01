package com.worksafe.backend.domain.alert.dto.response;

import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        RiskEventResponse riskEvent,
        WorkerResponse worker,
        String title,
        String message,
        AlertSeverity severity,
        AlertReadStatus readStatus,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
