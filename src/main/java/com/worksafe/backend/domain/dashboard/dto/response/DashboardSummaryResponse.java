package com.worksafe.backend.domain.dashboard.dto.response;

public record DashboardSummaryResponse(
        long totalWorkers,
        long normalWorkers,
        long warningWorkers,
        long dangerWorkers,
        long lv1RiskEvents,
        long lv2RiskEvents,
        long lv3RiskEvents,
        long lv4RiskEvents,
        long totalEquipment,
        long wornEquipment,
        long notWornEquipment,
        long activeRiskEvents,
        long unreadAlerts,
        long activeDroneDispatches,
        long emergencyKitDropped,
        long activeBuzzerCommands,
        long readyDrones,
        long flyingDrones
) {
}
