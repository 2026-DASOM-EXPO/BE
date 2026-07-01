package com.worksafe.backend.domain.alert.converter;

import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;

import java.util.List;

public final class AlertConverter {

    private AlertConverter() {
    }

    public static AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getRiskEvent() == null ? null : RiskEventConverter.toResponse(alert.getRiskEvent()),
                alert.getWorker() == null ? null : WorkerConverter.toResponse(alert.getWorker()),
                alert.getTitle(),
                alert.getMessage(),
                alert.getSeverity(),
                alert.getReadStatus(),
                alert.getCreatedAt(),
                alert.getReadAt()
        );
    }

    public static List<AlertResponse> toResponseList(List<Alert> alerts) {
        return alerts.stream().map(AlertConverter::toResponse).toList();
    }
}
