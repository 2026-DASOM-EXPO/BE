package com.worksafe.backend.domain.iot.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EquipmentStatusRequest(
        @NotNull Long workerId,
        @NotNull Long equipmentId,
        WearStatus wearStatus,
        @NotNull @Min(0) @Max(4095) Integer pressureValue,
        LocalDateTime measuredAt
) {
}
