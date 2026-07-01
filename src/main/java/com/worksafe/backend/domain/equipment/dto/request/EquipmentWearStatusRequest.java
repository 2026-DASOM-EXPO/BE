package com.worksafe.backend.domain.equipment.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.NotNull;

public record EquipmentWearStatusRequest(
        @NotNull WearStatus wearStatus
) {
}
