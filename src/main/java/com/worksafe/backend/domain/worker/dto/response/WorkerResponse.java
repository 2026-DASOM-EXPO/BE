package com.worksafe.backend.domain.worker.dto.response;

import com.worksafe.backend.domain.worker.enums.WorkerStatus;

import java.time.LocalDateTime;

public record WorkerResponse(
        Long id,
        String name,
        String department,
        String phoneNumber,
        String rfidTag,
        WorkerStatus status,
        Double currentLatitude,
        Double currentLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
