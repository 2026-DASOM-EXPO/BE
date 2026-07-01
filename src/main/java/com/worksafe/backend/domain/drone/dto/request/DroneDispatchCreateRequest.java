package com.worksafe.backend.domain.drone.dto.request;

import jakarta.validation.constraints.NotNull;

public record DroneDispatchCreateRequest(
        @NotNull Long riskEventId,
        Double targetLatitude,
        Double targetLongitude,
        String commandMessage
) {
}
