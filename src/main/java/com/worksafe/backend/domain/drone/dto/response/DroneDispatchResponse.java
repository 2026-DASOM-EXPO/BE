package com.worksafe.backend.domain.drone.dto.response;

import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.EmergencyCallStatus;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;

import java.time.LocalDateTime;

public record DroneDispatchResponse(
        Long id,
        DroneResponse drone,
        RiskEventResponse riskEvent,
        Double targetLatitude,
        Double targetLongitude,
        String dispatchReason,
        boolean emergencyKitMounted,
        boolean emergencyKitDropped,
        Double dropLatitude,
        Double dropLongitude,
        DropMethod dropMethod,
        boolean emergencyCallRequested,
        EmergencyCallStatus emergencyCallStatus,
        DroneDispatchStatus status,
        String commandMessage,
        LocalDateTime dispatchedAt,
        LocalDateTime arrivedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
