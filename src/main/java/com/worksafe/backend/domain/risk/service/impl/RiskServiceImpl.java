package com.worksafe.backend.domain.risk.service.impl;

import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.converter.DroneConverter;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneVideoRepository;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.request.RiskEventStatusUpdateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventReportResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.risk.service.RiskEvaluationService;
import com.worksafe.backend.domain.risk.service.RiskService;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskServiceImpl implements RiskService {

    private final RiskEventRepository riskEventRepository;
    private final WorkerRepository workerRepository;
    private final RiskEvaluationService riskEvaluationService;
    private final DroneDispatchRepository droneDispatchRepository;
    private final DroneVideoRepository droneVideoRepository;

    @Override
    public RiskEventResponse create(RiskEventCreateRequest request) {
        Worker worker = resolveWorker(request.workerId());
        RiskEvent riskEvent = riskEventRepository.save(RiskEventConverter.toEntity(request, worker));
        riskEvaluationService.handleRiskEvent(riskEvent);
        return RiskEventConverter.toResponse(riskEvent);
    }

    @Override
    public List<RiskEventResponse> findAll() {
        return RiskEventConverter.toResponseList(riskEventRepository.findAllByOrderByOccurredAtDesc());
    }

    @Override
    public RiskEventResponse findById(Long riskEventId) {
        return RiskEventConverter.toResponse(getRiskEvent(riskEventId));
    }

    @Override
    public RiskEventResponse updateStatus(Long riskEventId, RiskEventStatusUpdateRequest request) {
        RiskEvent riskEvent = getRiskEvent(riskEventId);
        riskEvent.changeStatus(request.status());
        if (riskEvent.getWorker() != null) {
            riskEvaluationService.evaluateWorkerRisk(riskEvent.getWorker().getId());
        }
        return RiskEventConverter.toResponse(riskEvent);
    }

    @Override
    public List<RiskEventResponse> findByWorkerId(Long workerId) {
        return RiskEventConverter.toResponseList(riskEventRepository.findByWorker_IdOrderByOccurredAtDesc(workerId));
    }

    @Override
    public List<RiskEventReportResponse> findReports(Long workerId, RiskLevel riskLevel, RiskStatus status, LocalDateTime from, LocalDateTime to) {
        return riskEventRepository.findAllByOrderByOccurredAtDesc().stream()
                .filter(event -> workerId == null || (event.getWorker() != null && workerId.equals(event.getWorker().getId())))
                .filter(event -> riskLevel == null || event.getRiskLevel() == riskLevel)
                .filter(event -> status == null || event.getStatus() == status)
                .filter(event -> from == null || event.getOccurredAt() == null || !event.getOccurredAt().isBefore(from))
                .filter(event -> to == null || event.getOccurredAt() == null || !event.getOccurredAt().isAfter(to))
                .map(event -> {
                    DroneDispatchResponse dispatchResponse = null;
                    DroneVideoResponse videoResponse = null;
                    var dispatch = droneDispatchRepository.findFirstByRiskEvent_IdOrderByCreatedAtDesc(event.getId());
                    if (dispatch != null) {
                        dispatchResponse = DroneConverter.toDispatchResponse(dispatch);
                        videoResponse = droneVideoRepository.findFirstByDispatch_IdOrderByCreatedAtDesc(dispatch.getId())
                                .map(DroneConverter::toVideoResponse)
                                .orElse(null);
                    }
                    return new RiskEventReportResponse(RiskEventConverter.toResponse(event), dispatchResponse, videoResponse);
                })
                .toList();
    }

    private RiskEvent getRiskEvent(Long riskEventId) {
        return riskEventRepository.findById(riskEventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVENT_NOT_FOUND));
    }

    private Worker resolveWorker(Long workerId) {
        if (workerId == null) {
            return null;
        }
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }
}
