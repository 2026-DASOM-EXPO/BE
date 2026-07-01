package com.worksafe.backend.domain.risk.converter;

import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;
import com.worksafe.backend.domain.worker.entity.Worker;

import java.util.List;

public final class RiskEventConverter {

    private RiskEventConverter() {
    }

    public static RiskEvent toEntity(RiskEventCreateRequest request, Worker worker) {
        return RiskEvent.builder()
                .worker(worker)
                .sourceType(request.sourceType())
                .riskType(request.riskType())
                .riskLevel(request.riskLevel())
                .description(request.description())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status(request.status())
                .occurredAt(request.occurredAt())
                .build();
    }

    public static RiskEventResponse toResponse(RiskEvent riskEvent) {
        return new RiskEventResponse(
                riskEvent.getId(),
                riskEvent.getWorker() == null ? null : WorkerConverter.toResponse(riskEvent.getWorker()),
                riskEvent.getSourceType(),
                riskEvent.getRiskType(),
                riskEvent.getRiskLevel(),
                riskEvent.getDescription(),
                riskEvent.getLatitude(),
                riskEvent.getLongitude(),
                riskEvent.getStatus(),
                riskEvent.getOccurredAt(),
                riskEvent.getResolvedAt(),
                riskEvent.getCreatedAt(),
                riskEvent.getUpdatedAt()
        );
    }

    public static List<RiskEventResponse> toResponseList(List<RiskEvent> riskEvents) {
        return riskEvents.stream().map(RiskEventConverter::toResponse).toList();
    }
}
