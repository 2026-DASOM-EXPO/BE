package com.worksafe.backend.domain.drone.converter;

import com.worksafe.backend.domain.drone.dto.response.DroneDropLogResponse;
import com.worksafe.backend.domain.drone.entity.DroneDropLog;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;

import java.util.List;

public final class DroneDropLogConverter {

    private DroneDropLogConverter() {
    }

    public static DroneDropLogResponse toResponse(DroneDropLog dropLog) {
        return new DroneDropLogResponse(
                dropLog.getId(),
                DroneConverter.toDispatchResponse(dropLog.getDroneDispatch()),
                DroneConverter.toResponse(dropLog.getDrone()),
                dropLog.getWorker() == null ? null : WorkerConverter.toResponse(dropLog.getWorker()),
                dropLog.getRiskEvent() == null ? null : RiskEventConverter.toResponse(dropLog.getRiskEvent()),
                dropLog.getDropMethod(),
                dropLog.getTargetLatitude(),
                dropLog.getTargetLongitude(),
                dropLog.getActualDropLatitude(),
                dropLog.getActualDropLongitude(),
                dropLog.isObstacleDetected(),
                dropLog.getLidarFrontLeft(),
                dropLog.getLidarFrontRight(),
                dropLog.getLidarBackLeft(),
                dropLog.getLidarBackRight(),
                dropLog.getLidarSideLeft(),
                dropLog.getLidarSideRight(),
                dropLog.getDropStatus(),
                dropLog.getCreatedAt(),
                dropLog.getUpdatedAt()
        );
    }

    public static List<DroneDropLogResponse> toResponseList(List<DroneDropLog> dropLogs) {
        return dropLogs.stream().map(DroneDropLogConverter::toResponse).toList();
    }
}
