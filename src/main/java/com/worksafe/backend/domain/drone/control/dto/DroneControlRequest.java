package com.worksafe.backend.domain.drone.control.dto;

import com.worksafe.backend.domain.drone.control.DroneControlCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DroneControlRequest(
        @NotNull DroneControlCommand command,
        @Positive Integer durationSeconds
) {
}
