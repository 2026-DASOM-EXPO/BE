package com.worksafe.backend.domain.equipment.service.impl;

import com.worksafe.backend.domain.equipment.converter.WearableCommandConverter;
import com.worksafe.backend.domain.equipment.dto.request.WearableCommandCreateRequest;
import com.worksafe.backend.domain.equipment.dto.response.WearableCommandResponse;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.domain.equipment.service.WearableCommandService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WearableCommandServiceImpl implements WearableCommandService {

    private static final List<WearableCommandStatus> PENDING_STATUSES = List.of(WearableCommandStatus.REQUESTED, WearableCommandStatus.SENT);

    private final WearableCommandRepository wearableCommandRepository;
    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public WearableCommandResponse create(WearableCommandCreateRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipment(request.equipmentId());
        validateWorkerEquipment(worker, equipment);

        WearableCommand command = wearableCommandRepository.save(WearableCommand.builder()
                .worker(worker)
                .equipment(equipment)
                .commandType(request.commandType())
                .commandStatus(WearableCommandStatus.REQUESTED)
                .reason(request.reason() == null ? request.commandType().name() : request.reason())
                .requestedAt(LocalDateTime.now())
                .build());

        return WearableCommandConverter.toResponse(command);
    }

    @Override
    public List<WearableCommandResponse> findPending() {
        return WearableCommandConverter.toResponseList(wearableCommandRepository.findByCommandStatusInOrderByCreatedAtAsc(PENDING_STATUSES));
    }

    @Override
    public WearableCommandResponse acknowledge(Long commandId) {
        WearableCommand command = wearableCommandRepository.findById(commandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEARABLE_COMMAND_NOT_FOUND));
        if (command.getCommandStatus() == WearableCommandStatus.ACKNOWLEDGED || command.getCommandStatus() == WearableCommandStatus.FAILED) {
            throw new BusinessException(ErrorCode.INVALID_COMMAND_STATUS);
        }
        command.acknowledge();
        return WearableCommandConverter.toResponse(command);
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private Equipment getEquipment(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    }

    private void validateWorkerEquipment(Worker worker, Equipment equipment) {
        if (equipment.getWorker() != null && !equipment.getWorker().getId().equals(worker.getId())) {
            throw new BusinessException(ErrorCode.DEVICE_WORKER_MISMATCH);
        }
    }
}
