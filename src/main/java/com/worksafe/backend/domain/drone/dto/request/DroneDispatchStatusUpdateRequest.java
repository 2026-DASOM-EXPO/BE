package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import jakarta.validation.constraints.NotNull;

public record DroneDispatchStatusUpdateRequest(
        @NotNull DroneDispatchStatus status
) {
}
