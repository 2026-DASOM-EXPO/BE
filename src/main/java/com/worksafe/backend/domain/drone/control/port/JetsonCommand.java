package com.worksafe.backend.domain.drone.control.port;

import com.worksafe.backend.domain.drone.control.DroneControlCommand;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JetsonCommand(
        UUID commandId,
        Long droneId,
        String droneSerialNumber,
        DroneControlCommand command,
        int durationSeconds,
        OffsetDateTime issuedAt
) {
}
