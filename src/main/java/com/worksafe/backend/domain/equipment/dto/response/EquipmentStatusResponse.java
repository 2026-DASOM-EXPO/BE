package com.worksafe.backend.domain.equipment.dto.response;

import java.time.LocalDateTime;

public record EquipmentStatusResponse(
        EquipmentResponse equipment,
        LocalDateTime issuedAt,
        LocalDateTime returnedAt
) {
}
