package com.worksafe.backend.domain.worker.dto.request;

import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkerCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 50) String phoneNumber,
        @NotBlank @Size(max = 100) String rfidTag,
        @NotNull WorkerStatus status,
        Double currentLatitude,
        Double currentLongitude
) {
}
