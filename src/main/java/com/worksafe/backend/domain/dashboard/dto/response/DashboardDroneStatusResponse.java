package com.worksafe.backend.domain.dashboard.dto.response;

public record DashboardDroneStatusResponse(
        long readyDrones,
        long flyingDrones,
        long returningDrones,
        long chargingDrones,
        long maintenanceDrones,
        long disabledDrones
) {
}
