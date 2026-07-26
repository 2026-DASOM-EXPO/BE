package com.worksafe.backend.domain.risk.service.impl;

import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchCreateRequest;
import com.worksafe.backend.domain.drone.converter.DroneConverter;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneVideoRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.drone.entity.DroneVideo;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.service.DroneService;
import com.worksafe.backend.domain.drone.enums.StreamStatus;
import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import com.worksafe.backend.domain.drone.streaming.DroneStreamGateway;
import com.worksafe.backend.domain.drone.streaming.DroneStreamingProperties;
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
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
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
    private final DroneStreamGateway droneStreamGateway;
    private final DroneStreamingProperties droneStreamingProperties;
    private final DroneRepository droneRepository;
    private final DroneService droneService;
    private final AlertRealtimeService alertRealtimeService;

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
        if (request.status() == RiskStatus.PROCESSING && riskEvent.getRiskType() == com.worksafe.backend.domain.risk.enums.RiskType.SOS_REQUEST) {
            startSosVideo(riskEvent);
        }
        if (riskEvent.getWorker() != null) {
            riskEvaluationService.evaluateWorkerRisk(riskEvent.getWorker().getId());
        }
        RiskEventResponse response = RiskEventConverter.toResponse(riskEvent);
        alertRealtimeService.publish("risk", response);
        return response;
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

    private void startSosVideo(RiskEvent riskEvent) {
        var dispatch = droneDispatchRepository.findFirstByRiskEvent_IdOrderByCreatedAtDesc(riskEvent.getId());
        if (dispatch == null) {
            var drone = droneRepository.findFirstByStatus(DroneStatus.READY)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));
            droneService.dispatch(drone.getId(), new DroneDispatchCreateRequest(
                    riskEvent.getId(),
                    riskEvent.getLatitude(),
                    riskEvent.getLongitude(),
                    "관리자 확인 SOS 수동 출동"
            ));
            return;
        }
        String streamKey = dispatch.getDrone().getSerialNumber();
        String playlistUrl = droneStreamGateway.playlistUrl(streamKey);
        DroneVideo video = droneVideoRepository.findFirstByDispatch_IdOrderByCreatedAtDesc(dispatch.getId())
                .orElseGet(() -> droneVideoRepository.save(DroneVideo.builder()
                        .drone(dispatch.getDrone())
                        .dispatch(dispatch)
                        .title("SOS 현장 영상")
                        .description("관리자 확인 후 시작된 720p 현장 영상")
                        .streamUrl(playlistUrl)
                        .protocol(VideoProtocol.HLS)
                        .active(false)
                        .streamStatus(StreamStatus.READY)
                        .width(droneStreamingProperties.targetWidth())
                        .height(droneStreamingProperties.targetHeight())
                        .frameRate(droneStreamingProperties.targetFrameRate())
                        .build()));
        video.configureStream(
                playlistUrl,
                droneStreamingProperties.targetWidth(),
                droneStreamingProperties.targetHeight(),
                droneStreamingProperties.targetFrameRate()
        );
        try {
            // 서버 재시작 뒤 DB에는 active=true가 남아 있어도 FFmpeg 프로세스는 사라지므로 항상 확인한다.
            droneStreamGateway.start(streamKey);
            video.start();
        } catch (RuntimeException e) {
            video.fail();
            throw e;
        }
    }

    private Worker resolveWorker(Long workerId) {
        if (workerId == null) {
            return null;
        }
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }
}
