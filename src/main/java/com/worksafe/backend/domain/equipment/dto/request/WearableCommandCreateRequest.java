package com.worksafe.backend.domain.equipment.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WearableCommandCreateRequest(
        @NotNull Long workerId,
        @NotNull Long equipmentId,
        @NotNull WearableCommandType commandType,
        @Size(max = 500) String reason
) {
}
