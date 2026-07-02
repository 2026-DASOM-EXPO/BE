package com.worksafe.backend.domain.iot.dto.response;

import com.worksafe.backend.domain.worker.enums.WorkerStatus;

public record AttendanceResponse(
        Long workerId,
        String name,
        WorkerStatus status
) {
}
