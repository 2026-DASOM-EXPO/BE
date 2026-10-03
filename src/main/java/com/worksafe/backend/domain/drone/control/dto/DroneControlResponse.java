package com.worksafe.backend.domain.drone.control.dto;

import com.worksafe.backend.domain.drone.control.DroneControlCommand;
import com.worksafe.backend.domain.drone.control.JetsonDeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DroneControlResponse(
        UUID commandId,
        Long droneId,
        String droneSerialNumber,
        DroneControlCommand command,
        int durationSeconds,
        OffsetDateTime issuedAt,
        JetsonDeliveryStatus jetsonDeliveryStatus,
        String deliveryDetail
) {
}
