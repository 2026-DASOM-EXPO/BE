package com.worksafe.backend.domain.drone.dto.response;

import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record DroneDropLogResponse(
        Long id,
        DroneDispatchResponse droneDispatch,
        DroneResponse drone,
        WorkerResponse worker,
        RiskEventResponse riskEvent,
        DropMethod dropMethod,
        Double targetLatitude,
        Double targetLongitude,
        Double actualDropLatitude,
        Double actualDropLongitude,
        boolean obstacleDetected,
        Double lidarFrontLeft,
        Double lidarFrontRight,
        Double lidarBackLeft,
        Double lidarBackRight,
        Double lidarSideLeft,
        Double lidarSideRight,
        DropStatus dropStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
