package com.worksafe.backend.domain.equipment.converter;

import com.worksafe.backend.domain.equipment.dto.response.EquipmentLogResponse;
import com.worksafe.backend.domain.equipment.entity.EquipmentLog;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;

import java.util.List;

public final class EquipmentLogConverter {

    private EquipmentLogConverter() {
    }

    public static EquipmentLogResponse toResponse(EquipmentLog equipmentLog) {
        return new EquipmentLogResponse(
                equipmentLog.getId(),
                equipmentLog.getWorker() == null ? null : WorkerConverter.toResponse(equipmentLog.getWorker()),
                equipmentLog.getEquipment() == null ? null : EquipmentConverter.toResponse(equipmentLog.getEquipment()),
                equipmentLog.getWearStatus(),
                equipmentLog.getIssuedAt(),
                equipmentLog.getReturnedAt(),
                equipmentLog.getCreatedAt(),
                equipmentLog.getUpdatedAt()
        );
    }

    public static List<EquipmentLogResponse> toResponseList(List<EquipmentLog> equipmentLogs) {
        return equipmentLogs.stream().map(EquipmentLogConverter::toResponse).toList();
    }
}
