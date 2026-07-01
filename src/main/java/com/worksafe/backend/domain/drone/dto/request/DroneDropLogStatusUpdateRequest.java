package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.DropStatus;
import jakarta.validation.constraints.NotNull;

public record DroneDropLogStatusUpdateRequest(
        @NotNull DropStatus dropStatus,
        Double actualDropLatitude,
        Double actualDropLongitude
) {
}
