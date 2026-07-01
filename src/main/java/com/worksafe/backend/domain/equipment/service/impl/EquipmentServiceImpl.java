package com.worksafe.backend.domain.equipment.service.impl;

import com.worksafe.backend.domain.equipment.converter.EquipmentConverter;
import com.worksafe.backend.domain.equipment.converter.EquipmentLogConverter;
import com.worksafe.backend.domain.equipment.dto.request.BuzzerControlRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentCreateRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentUpdateRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.request.ManualWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.request.WorkTimerRequest;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentLogResponse;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentResponse;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentStatusResponse;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.EquipmentLog;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.repository.EquipmentLogRepository;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.domain.equipment.service.EquipmentService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentLogRepository equipmentLogRepository;
    private final WorkerRepository workerRepository;
    private final WearableCommandRepository wearableCommandRepository;

    @Override
    public EquipmentResponse create(EquipmentCreateRequest request) {
        Worker worker = request.workerId() == null ? null : getWorker(request.workerId());
        Equipment equipment = EquipmentConverter.toEntity(request, worker);
        return EquipmentConverter.toResponse(equipmentRepository.save(equipment));
    }

    @Override
    public List<EquipmentResponse> findAll() {
        return EquipmentConverter.toResponseList(equipmentRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public EquipmentResponse findById(Long equipmentId) {
        return EquipmentConverter.toResponse(getEquipment(equipmentId));
    }

    @Override
    public EquipmentResponse update(Long equipmentId, EquipmentUpdateRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.update(
                request.serialNumber(),
                request.name(),
                request.type(),
                request.status(),
                request.wearStatus(),
                equipment.getLastDetectedAt()
        );
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public void delete(Long equipmentId) {
        equipmentRepository.delete(getEquipment(equipmentId));
    }

    @Override
    public EquipmentResponse assign(Long equipmentId, Long workerId) {
        Equipment equipment = getEquipment(equipmentId);
        Worker worker = getWorker(workerId);
        closeOpenLog(equipment, LocalDateTime.now());
        equipment.assignTo(worker);
        equipmentLogRepository.save(EquipmentLog.builder()
                .worker(worker)
                .equipment(equipment)
                .wearStatus(equipment.getWearStatus())
                .issuedAt(LocalDateTime.now())
                .build());
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public EquipmentResponse updateWearStatus(Long equipmentId, EquipmentWearStatusRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.updateWearStatus(request.wearStatus(), LocalDateTime.now());
        if (request.wearStatus() == WearStatus.NOT_WORN) {
            closeOpenLog(equipment, LocalDateTime.now());
        }
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public EquipmentResponse updateManualWearStatus(Long equipmentId, ManualWearStatusRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.setManualWearStatus(request.wearStatus());
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public EquipmentResponse updateBuzzer(Long equipmentId, BuzzerControlRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.setBuzzerEnabled(Boolean.TRUE.equals(request.enabled()));
        if (equipment.getWorker() != null) {
            createWearableCommand(
                    equipment,
                    equipment.getWorker(),
                    Boolean.TRUE.equals(request.enabled()) ? WearableCommandType.BUZZER_ON : WearableCommandType.BUZZER_OFF,
                    request.reason()
            );
        }
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public EquipmentResponse startWorkTimer(Long equipmentId, WorkTimerRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.startWorkTimer(LocalDateTime.now());
        if (equipment.getWorker() != null) {
            createWearableCommand(equipment, equipment.getWorker(), WearableCommandType.TIMER_START, request.reason());
        }
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public EquipmentResponse stopWorkTimer(Long equipmentId, WorkTimerRequest request) {
        Equipment equipment = getEquipment(equipmentId);
        equipment.stopWorkTimer(LocalDateTime.now());
        if (equipment.getWorker() != null) {
            createWearableCommand(equipment, equipment.getWorker(), WearableCommandType.TIMER_STOP, request.reason());
        }
        return EquipmentConverter.toResponse(equipment);
    }

    @Override
    public List<EquipmentStatusResponse> findStatus() {
        return equipmentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(equipment -> {
                    EquipmentLog latestLog = equipmentLogRepository.findTopByEquipment_IdOrderByIssuedAtDesc(equipment.getId()).orElse(null);
                    return new EquipmentStatusResponse(
                            EquipmentConverter.toResponse(equipment),
                            latestLog == null ? null : latestLog.getIssuedAt(),
                            latestLog == null ? null : latestLog.getReturnedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<EquipmentLogResponse> findLogs(Long workerId, Long equipmentId, LocalDateTime from, LocalDateTime to) {
        List<EquipmentLog> logs = equipmentLogRepository.findAllByOrderByIssuedAtDesc();
        return EquipmentLogConverter.toResponseList(logs.stream()
                .filter(log -> workerId == null || (log.getWorker() != null && workerId.equals(log.getWorker().getId())))
                .filter(log -> equipmentId == null || (log.getEquipment() != null && equipmentId.equals(log.getEquipment().getId())))
                .filter(log -> from == null || log.getIssuedAt() == null || !log.getIssuedAt().isBefore(from))
                .filter(log -> to == null || log.getIssuedAt() == null || !log.getIssuedAt().isAfter(to))
                .collect(Collectors.toList()));
    }

    private Equipment getEquipment(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private void createWearableCommand(Equipment equipment, Worker worker, WearableCommandType commandType, String reason) {
        wearableCommandRepository.save(WearableCommand.builder()
                .equipment(equipment)
                .worker(worker)
                .commandType(commandType)
                .commandStatus(WearableCommandStatus.REQUESTED)
                .reason(reason == null ? commandType.name() : reason)
                .requestedAt(LocalDateTime.now())
                .build());
    }

    private void closeOpenLog(Equipment equipment, LocalDateTime returnedAt) {
        equipmentLogRepository.findTopByEquipment_IdAndReturnedAtIsNullOrderByIssuedAtDesc(equipment.getId())
                .ifPresent(log -> {
                    log.markReturned(returnedAt);
                });
    }
}
