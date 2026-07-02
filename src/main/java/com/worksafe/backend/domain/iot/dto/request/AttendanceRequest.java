package com.worksafe.backend.domain.iot.dto.request;

import com.worksafe.backend.domain.equipment.enums.AttendanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AttendanceRequest(
        @NotBlank String rfidTag,
        @NotNull AttendanceType attendanceType,
        LocalDateTime measuredAt
) {
}
