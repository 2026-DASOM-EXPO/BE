package com.worksafe.backend.domain.iot.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

public record EquipmentStatusRequest(
        @NotNull Long workerId,
        @NotNull Long equipmentId,
        WearStatus wearStatus,
        @Min(0) @Max(4095) Integer lightValue
) {
    public EquipmentStatusRequest(Long workerId, Long equipmentId, WearStatus wearStatus) {
        this(workerId, equipmentId, wearStatus, null);
    }

    @AssertTrue(message = "wearStatus 또는 lightValue 중 하나는 필수입니다.")
    public boolean hasWearSignal() {
        return wearStatus != null || lightValue != null;
    }
}
