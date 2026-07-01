package com.worksafe.backend.domain.equipment.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManualWearStatusRequest(
        @NotNull WearStatus wearStatus,
        @Size(max = 500) String reason
) {
}
