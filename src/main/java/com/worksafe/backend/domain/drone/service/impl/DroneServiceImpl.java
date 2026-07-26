package com.worksafe.backend.domain.drone.service.impl;

import com.worksafe.backend.domain.drone.converter.DroneConverter;
import com.worksafe.backend.domain.drone.dto.request.DroneCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneVideoCreateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.entity.DroneVideo;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.EmergencyCallStatus;
import com.worksafe.backend.domain.drone.enums.StreamStatus;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.drone.repository.DroneVideoRepository;
import com.worksafe.backend.domain.drone.service.DroneService;
import com.worksafe.backend.domain.drone.streaming.DroneStreamGateway;
import com.worksafe.backend.domain.drone.streaming.DroneStreamingProperties;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DroneServiceImpl implements DroneService {

    private final DroneRepository droneRepository;
    private final DroneDispatchRepository dispatchRepository;
    private final DroneVideoRepository videoRepository;
    private final RiskEventRepository riskEventRepository;
    private final DroneStreamGateway droneStreamGateway;
    private final DroneStreamingProperties droneStreamingProperties;
    private final AlertRealtimeService alertRealtimeService;

    @Override
    public DroneResponse create(DroneCreateRequest request) {
        Drone drone = DroneConverter.toEntity(request);
        Drone saved = droneRepository.save(drone);
        publishDrone(saved);
        return DroneConverter.toResponse(saved);
    }

    @Override
    public List<DroneResponse> findAll() {
        return DroneConverter.toResponseList(droneRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public DroneResponse findById(Long droneId) {
        return DroneConverter.toResponse(getDrone(droneId));
    }

    @Override
    public DroneResponse update(Long droneId, DroneUpdateRequest request) {
        Drone drone = getDrone(droneId);
        drone.update(
                request.name(),
                request.serialNumber(),
                request.modelName(),
                request.status(),
                request.batteryPercent(),
                request.currentLatitude(),
                request.currentLongitude(),
                request.maxFlightMinutes(),
                request.payloadMounted()
        );
        publishDrone(drone);
        return DroneConverter.toResponse(drone);
    }

    @Override
    public void delete(Long droneId) {
        droneRepository.delete(getDrone(droneId));
        alertRealtimeService.publish("drone-deleted", Map.of("id", droneId));
    }

    @Override
    public DroneDispatchResponse dispatch(Long droneId, DroneDispatchCreateRequest request) {
        Drone drone = getDrone(droneId);
        if (drone.getStatus() != DroneStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_DRONE_STATUS);
        }

        RiskEvent riskEvent = riskEventRepository.findById(request.riskEventId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVENT_NOT_FOUND));

        DroneDispatch dispatch = dispatchRepository.save(DroneDispatch.builder()
                .drone(drone)
                .riskEvent(riskEvent)
                .targetLatitude(request.targetLatitude() != null ? request.targetLatitude() : riskEvent.getLatitude())
                .targetLongitude(request.targetLongitude() != null ? request.targetLongitude() : riskEvent.getLongitude())
                .dispatchReason("수동")
                .emergencyKitMounted(true)
                .emergencyKitDropped(false)
                .dropMethod(DropMethod.MANUAL)
                .emergencyCallRequested(false)
                .emergencyCallStatus(EmergencyCallStatus.NOT_REQUESTED)
                .status(DroneDispatchStatus.DISPATCHED)
                .commandMessage(request.commandMessage() == null ? "수동 출동 요청" : request.commandMessage())
                .dispatchedAt(LocalDateTime.now())
                .build());

        drone.changeStatus(DroneStatus.FLYING);
        DroneVideo video = startDispatchVideo(dispatch);
        publishDrone(drone);
        alertRealtimeService.publish("dispatch", DroneConverter.toDispatchResponse(dispatch));
        alertRealtimeService.publish("video", DroneConverter.toVideoResponse(video));
        return DroneConverter.toDispatchResponse(dispatch);
    }

    @Override
    public List<DroneDispatchResponse> findAllDispatches() {
        return DroneConverter.toDispatchResponseList(dispatchRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public DroneDispatchResponse findDispatchById(Long dispatchId) {
        return DroneConverter.toDispatchResponse(getDispatch(dispatchId));
    }

    @Override
    public DroneDispatchResponse updateDispatchStatus(Long dispatchId, DroneDispatchStatusUpdateRequest request) {
        DroneDispatch dispatch = getDispatch(dispatchId);
        dispatch.changeStatus(request.status());

        if (request.status() == DroneDispatchStatus.RETURNED
                || request.status() == DroneDispatchStatus.FAILED
                || request.status() == DroneDispatchStatus.CANCELED) {
            dispatch.getDrone().changeStatus(DroneStatus.READY);
            videoRepository.findFirstByDispatch_IdOrderByCreatedAtDesc(dispatch.getId())
                    .ifPresent(video -> {
                        droneStreamGateway.stop(video.getDrone().getSerialNumber());
                        video.stop();
                        alertRealtimeService.publish("video", DroneConverter.toVideoResponse(video));
                    });
        }
        if (request.status() == DroneDispatchStatus.ARRIVED
                || request.status() == DroneDispatchStatus.KIT_DROPPED) {
            dispatch.getDrone().changeStatus(DroneStatus.FLYING);
        }

        publishDrone(dispatch.getDrone());
        alertRealtimeService.publish("dispatch", DroneConverter.toDispatchResponse(dispatch));
        return DroneConverter.toDispatchResponse(dispatch);
    }

    @Override
    public DroneVideoResponse createVideo(Long droneId, DroneVideoCreateRequest request) {
        Drone drone = getDrone(droneId);
        DroneVideo video = videoRepository.save(DroneVideo.builder()
                .drone(drone)
                .dispatch(request.dispatchId() == null ? null : getDispatch(request.dispatchId()))
                .title(request.title() == null ? drone.getName() + " video" : request.title())
                .description(request.description())
                .streamUrl(request.streamUrl())
                .protocol(request.protocol())
                .active(false)
                .streamStatus(StreamStatus.READY)
                .width(request.width())
                .height(request.height())
                .frameRate(request.frameRate())
                .build());

        return DroneConverter.toVideoResponse(video);
    }

    @Override
    public DroneVideoResponse startVideo(Long videoId) {
        DroneVideo video = getVideo(videoId);
        videoRepository.findFirstByDrone_IdAndActiveTrue(video.getDrone().getId())
                .filter(activeVideo -> !activeVideo.getId().equals(videoId))
                .ifPresent(DroneVideo::deactivate);
        String streamKey = video.getDrone().getSerialNumber();
        video.configureStream(
                droneStreamGateway.playlistUrl(streamKey),
                droneStreamingProperties.targetWidth(),
                droneStreamingProperties.targetHeight(),
                droneStreamingProperties.targetFrameRate()
        );
        try {
            droneStreamGateway.start(streamKey);
            video.start();
        } catch (RuntimeException e) {
            video.fail();
            throw e;
        }
        DroneVideoResponse response = DroneConverter.toVideoResponse(video);
        alertRealtimeService.publish("video", response);
        return response;
    }

    @Override
    public DroneVideoResponse stopVideo(Long videoId) {
        DroneVideo video = getVideo(videoId);
        droneStreamGateway.stop(video.getDrone().getSerialNumber());
        video.stop();
        DroneVideoResponse response = DroneConverter.toVideoResponse(video);
        alertRealtimeService.publish("video", response);
        return response;
    }

    @Override
    public DroneVideoResponse findActiveVideo(Long droneId) {
        DroneVideo video = videoRepository.findFirstByDrone_IdAndActiveTrue(droneId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));
        DroneVideoResponse response = DroneConverter.toVideoResponse(video);
        alertRealtimeService.publish("video", response);
        return response;
    }

    private Drone getDrone(Long droneId) {
        return droneRepository.findById(droneId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));
    }

    private DroneDispatch getDispatch(Long dispatchId) {
        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_DISPATCH_NOT_FOUND));
    }

    private DroneVideo getVideo(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));
    }

    private DroneVideo startDispatchVideo(DroneDispatch dispatch) {
        Drone drone = dispatch.getDrone();
        String streamKey = drone.getSerialNumber();
        String playlistUrl = droneStreamGateway.playlistUrl(streamKey);
        DroneVideo video = videoRepository.save(DroneVideo.builder()
                .drone(drone)
                .dispatch(dispatch)
                .title("현장 실시간 영상")
                .description("관리자 수동 출동과 함께 시작된 720p 영상")
                .streamUrl(playlistUrl)
                .protocol(com.worksafe.backend.domain.drone.enums.VideoProtocol.HLS)
                .active(false)
                .streamStatus(StreamStatus.READY)
                .width(droneStreamingProperties.targetWidth())
                .height(droneStreamingProperties.targetHeight())
                .frameRate(droneStreamingProperties.targetFrameRate())
                .build());
        try {
            droneStreamGateway.start(streamKey);
            video.start();
        } catch (RuntimeException exception) {
            video.fail();
        }
        return video;
    }

    private void publishDrone(Drone drone) {
        alertRealtimeService.publish("drone", DroneConverter.toResponse(drone));
    }
}
