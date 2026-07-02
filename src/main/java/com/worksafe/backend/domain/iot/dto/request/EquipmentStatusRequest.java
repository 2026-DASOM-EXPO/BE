package com.worksafe.backend.domain.iot.dto.request;

import com.worksafe.backend.equipment.enums.WearStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EquipmentStatusRequest(
        @NotNull Long workerId,
        @NotNull Long equipmentId,
        @NotNull WearStatus wearStatus,
        Double pressureValue,
        LocalDateTime measuredAt
) {
}
