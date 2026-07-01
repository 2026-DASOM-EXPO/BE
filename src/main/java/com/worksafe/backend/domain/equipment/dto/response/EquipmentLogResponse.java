package com.worksafe.backend.domain.equipment.dto.response;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record EquipmentLogResponse(
        Long id,
        WorkerResponse worker,
        EquipmentResponse equipment,
        WearStatus wearStatus,
        LocalDateTime issuedAt,
        LocalDateTime returnedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
