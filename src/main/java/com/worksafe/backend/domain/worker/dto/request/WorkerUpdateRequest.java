package com.worksafe.backend.domain.worker.dto.request;

import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import jakarta.validation.constraints.Size;

public record WorkerUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 100) String department,
        @Size(max = 50) String phoneNumber,
        @Size(max = 100) String rfidTag,
        WorkerStatus status,
        Double currentLatitude,
        Double currentLongitude
) {
}
