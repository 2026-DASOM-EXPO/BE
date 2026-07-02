package com.worksafe.backend.domain.dashboard.dto.response;

public record DashboardEquipmentStatusResponse(
        long totalEquipment,
        long wornEquipment,
        long notWornEquipment,
        long unknownEquipment
) {
}
