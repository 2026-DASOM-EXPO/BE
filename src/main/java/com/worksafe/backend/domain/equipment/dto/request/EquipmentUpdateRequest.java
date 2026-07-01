package com.worksafe.backend.domain.equipment.dto.request;

import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.Size;

public record EquipmentUpdateRequest(
        @Size(max = 100) String serialNumber,
        @Size(max = 100) String name,
        EquipmentType type,
        EquipmentStatus status,
        WearStatus wearStatus
) {
}
