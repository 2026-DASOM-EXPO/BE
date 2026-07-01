package com.worksafe.backend.domain.equipment.dto.response;

import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record EquipmentResponse(
        Long id,
        WorkerResponse worker,
        String serialNumber,
        String name,
        EquipmentType type,
        EquipmentStatus status,
        WearStatus wearStatus,
        LocalDateTime lastDetectedAt,
        boolean buzzerEnabled,
        boolean workTimerEnabled,
        LocalDateTime workTimerStartedAt,
        LocalDateTime workTimerEndedAt,
        boolean manualWearOverride,
        WearStatus manualWearStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
