package com.worksafe.backend.domain.equipment.dto.response;

import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record WearableCommandResponse(
        Long id,
        WorkerResponse worker,
        EquipmentResponse equipment,
        WearableCommandType commandType,
        WearableCommandStatus commandStatus,
        String reason,
        LocalDateTime requestedAt,
        LocalDateTime acknowledgedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
