package com.worksafe.backend.domain.risk.dto.response;

import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;

public record RiskEventReportResponse(
        RiskEventResponse riskEvent,
        DroneDispatchResponse droneDispatch,
        DroneVideoResponse droneVideo
) {
}
