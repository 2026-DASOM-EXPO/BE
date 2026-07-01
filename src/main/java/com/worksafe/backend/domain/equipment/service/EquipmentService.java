package com.worksafe.backend.domain.equipment.service;

import com.worksafe.backend.domain.equipment.dto.request.EquipmentCreateRequest;
import com.worksafe.backend.domain.equipment.dto.request.BuzzerControlRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentUpdateRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.request.ManualWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.request.WorkTimerRequest;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentLogResponse;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentResponse;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentStatusResponse;

import java.util.List;
import java.time.LocalDateTime;

public interface EquipmentService {

    EquipmentResponse create(EquipmentCreateRequest request);

    List<EquipmentResponse> findAll();

    EquipmentResponse findById(Long equipmentId);

    EquipmentResponse update(Long equipmentId, EquipmentUpdateRequest request);

    void delete(Long equipmentId);

    EquipmentResponse assign(Long equipmentId, Long workerId);

    EquipmentResponse updateWearStatus(Long equipmentId, EquipmentWearStatusRequest request);

    EquipmentResponse updateManualWearStatus(Long equipmentId, ManualWearStatusRequest request);

    EquipmentResponse updateBuzzer(Long equipmentId, BuzzerControlRequest request);

    EquipmentResponse startWorkTimer(Long equipmentId, WorkTimerRequest request);

    EquipmentResponse stopWorkTimer(Long equipmentId, WorkTimerRequest request);

    List<EquipmentStatusResponse> findStatus();

    List<EquipmentLogResponse> findLogs(Long workerId, Long equipmentId, LocalDateTime from, LocalDateTime to);
}
