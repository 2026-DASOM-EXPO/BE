package com.worksafe.backend.domain.equipment.dto.request;

import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipmentCreateRequest(
        Long workerId,
        @NotBlank @Size(max = 100) String serialNumber,
        @NotBlank @Size(max = 100) String name,
        @NotNull EquipmentType type,
        EquipmentStatus status,
        WearStatus wearStatus
) {
}
