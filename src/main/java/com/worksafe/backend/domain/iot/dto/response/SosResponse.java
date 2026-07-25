package com.worksafe.backend.domain.iot.dto.response;

import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;

public record SosResponse(
        Integer buttonValue,
        boolean triggered,
        RiskEventResponse riskEvent,
        DroneDispatchResponse droneDispatch,
        boolean alertCreated,
        boolean emergencyCallRequested
) {
}
