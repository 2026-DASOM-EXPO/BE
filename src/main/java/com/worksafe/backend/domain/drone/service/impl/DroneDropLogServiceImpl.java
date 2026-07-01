package com.worksafe.backend.domain.drone.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.drone.converter.DroneDropLogConverter;
import com.worksafe.backend.domain.drone.dto.request.DroneDropLogCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDropLogStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDropLogResponse;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.entity.DroneDropLog;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneDropLogRepository;
import com.worksafe.backend.domain.drone.service.DroneDropLogService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DroneDropLogServiceImpl implements DroneDropLogService {

    private final DroneDropLogRepository droneDropLogRepository;
    private final DroneDispatchRepository droneDispatchRepository;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;

    @Override
    public DroneDropLogResponse create(Long dispatchId, DroneDropLogCreateRequest request) {
        DroneDispatch dispatch = getDispatch(dispatchId);
        DroneDropLog dropLog = droneDropLogRepository.save(DroneDropLog.builder()
                .droneDispatch(dispatch)
                .drone(dispatch.getDrone())
                .worker(dispatch.getRiskEvent() == null ? null : dispatch.getRiskEvent().getWorker())
                .riskEvent(dispatch.getRiskEvent())
                .dropMethod(request.dropMethod())
                .targetLatitude(request.targetLatitude() != null ? request.targetLatitude() : dispatch.getTargetLatitude())
                .targetLongitude(request.targetLongitude() != null ? request.targetLongitude() : dispatch.getTargetLongitude())
                .actualDropLatitude(request.actualDropLatitude())
                .actualDropLongitude(request.actualDropLongitude())
                .obstacleDetected(Boolean.TRUE.equals(request.obstacleDetected()))
                .lidarFrontLeft(request.lidarFrontLeft())
                .lidarFrontRight(request.lidarFrontRight())
                .lidarBackLeft(request.lidarBackLeft())
                .lidarBackRight(request.lidarBackRight())
                .lidarSideLeft(request.lidarSideLeft())
                .lidarSideRight(request.lidarSideRight())
                .dropStatus(request.dropStatus() == null ? DropStatus.READY : request.dropStatus())
                .build());

        applyDispatchState(dispatch, dropLog, request.actualDropLatitude(), request.actualDropLongitude());
        return DroneDropLogConverter.toResponse(dropLog);
    }

    @Override
    public List<DroneDropLogResponse> findByDispatchId(Long dispatchId) {
        getDispatch(dispatchId);
        return DroneDropLogConverter.toResponseList(droneDropLogRepository.findByDroneDispatch_IdOrderByCreatedAtDesc(dispatchId));
    }

    @Override
    public DroneDropLogResponse updateStatus(Long dropLogId, DroneDropLogStatusUpdateRequest request) {
        DroneDropLog dropLog = droneDropLogRepository.findById(dropLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_DROP_LOG_NOT_FOUND));
        dropLog.changeStatus(request.dropStatus());
        dropLog.updateActualPosition(request.actualDropLatitude(), request.actualDropLongitude());
        applyDispatchState(dropLog.getDroneDispatch(), dropLog, request.actualDropLatitude(), request.actualDropLongitude());
        return DroneDropLogConverter.toResponse(dropLog);
    }

    private void applyDispatchState(DroneDispatch dispatch, DroneDropLog dropLog, Double actualDropLatitude, Double actualDropLongitude) {
        if (dropLog.isObstacleDetected() || dropLog.getDropStatus() == DropStatus.FAILED) {
            dispatch.changeStatus(DroneDispatchStatus.FAILED);
            dispatch.getDrone().changeStatus(DroneStatus.READY);
            Alert alert = alertRepository.save(Alert.builder()
                    .riskEvent(dispatch.getRiskEvent())
                    .worker(dispatch.getRiskEvent() == null ? null : dispatch.getRiskEvent().getWorker())
                    .title("Drop failed")
                    .message("Emergency kit drop failed or was blocked.")
                    .severity(AlertSeverity.WARNING)
                    .readStatus(AlertReadStatus.UNREAD)
                    .build());
            alertRealtimeService.publish(AlertConverter.toResponse(alert));
            return;
        }

        if (dropLog.getDropStatus() == DropStatus.DROPPED) {
            dispatch.markKitDropped(
                    actualDropLatitude != null ? actualDropLatitude : dropLog.getActualDropLatitude(),
                    actualDropLongitude != null ? actualDropLongitude : dropLog.getActualDropLongitude(),
                    dropLog.getDropMethod()
            );
            Alert alert = alertRepository.save(Alert.builder()
                    .riskEvent(dispatch.getRiskEvent())
                    .worker(dispatch.getRiskEvent() == null ? null : dispatch.getRiskEvent().getWorker())
                    .title("Emergency kit dropped")
                    .message("Emergency kit delivery completed.")
                    .severity(AlertSeverity.INFO)
                    .readStatus(AlertReadStatus.UNREAD)
                    .build());
            alertRealtimeService.publish(AlertConverter.toResponse(alert));
        }
    }

    private DroneDispatch getDispatch(Long dispatchId) {
        return droneDispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_DISPATCH_NOT_FOUND));
    }
}
