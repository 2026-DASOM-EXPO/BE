package com.worksafe.backend.domain.equipment.converter;

import com.worksafe.backend.domain.equipment.dto.response.WearableCommandResponse;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;

import java.util.List;

public final class WearableCommandConverter {

    private WearableCommandConverter() {
    }

    public static WearableCommandResponse toResponse(WearableCommand command) {
        return new WearableCommandResponse(
                command.getId(),
                command.getWorker() == null ? null : WorkerConverter.toResponse(command.getWorker()),
                command.getEquipment() == null ? null : EquipmentConverter.toResponse(command.getEquipment()),
                command.getCommandType(),
                command.getCommandStatus(),
                command.getReason(),
                command.getRequestedAt(),
                command.getAcknowledgedAt(),
                command.getCreatedAt(),
                command.getUpdatedAt()
        );
    }

    public static List<WearableCommandResponse> toResponseList(List<WearableCommand> commands) {
        return commands.stream().map(WearableCommandConverter::toResponse).toList();
    }
}
