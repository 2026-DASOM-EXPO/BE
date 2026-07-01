package com.worksafe.backend.domain.equipment.converter;

import com.worksafe.backend.domain.equipment.dto.request.EquipmentCreateRequest;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentResponse;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;
import com.worksafe.backend.domain.worker.entity.Worker;

import java.util.List;

public final class EquipmentConverter {

    private EquipmentConverter() {
    }

    public static Equipment toEntity(EquipmentCreateRequest request, Worker worker) {
        return Equipment.builder()
                .worker(worker)
                .serialNumber(request.serialNumber())
                .name(request.name())
                .type(request.type())
                .status(request.status())
                .wearStatus(request.wearStatus())
                .build();
    }

    public static EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getWorker() == null ? null : WorkerConverter.toResponse(equipment.getWorker()),
                equipment.getSerialNumber(),
                equipment.getName(),
                equipment.getType(),
                equipment.getStatus(),
                equipment.getWearStatus(),
                equipment.getLastDetectedAt(),
                equipment.isBuzzerEnabled(),
                equipment.isWorkTimerEnabled(),
                equipment.getWorkTimerStartedAt(),
                equipment.getWorkTimerEndedAt(),
                equipment.isManualWearOverride(),
                equipment.getManualWearStatus(),
                equipment.getCreatedAt(),
                equipment.getUpdatedAt()
        );
    }

    public static List<EquipmentResponse> toResponseList(List<Equipment> equipments) {
        return equipments.stream().map(EquipmentConverter::toResponse).toList();
    }
}
