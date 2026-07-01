package com.worksafe.backend.domain.sensor.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.service.RiskService;
import com.worksafe.backend.domain.sensor.converter.SensorLogConverter;
import com.worksafe.backend.domain.sensor.dto.request.SensorLogCreateRequest;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.sensor.repository.SensorLogRepository;
import com.worksafe.backend.domain.sensor.service.SensorLogService;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorLogServiceImpl implements SensorLogService {

    private final SensorLogRepository sensorLogRepository;
    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;
    private final RiskService riskService;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;

    @Override
    public SensorLogResponse create(SensorLogCreateRequest request) {
        Worker worker = resolveWorker(request.workerId());
        Equipment equipment = resolveEquipment(request.equipmentId());

        SensorLog sensorLog = SensorLogConverter.toEntity(
                request,
                worker,
                equipment
        );
        SensorLog saved = sensorLogRepository.save(sensorLog);

        if (request.latitude() != null && request.longitude() != null && worker != null) {
            worker.updateLocation(request.latitude(), request.longitude());
        }

        if (request.wearStatus() != null && equipment != null) {
            equipment.updateWearStatus(request.wearStatus(), LocalDateTime.now());
        }

        if (Boolean.TRUE.equals(request.sosPressed()) && worker != null) {
            riskService.create(new RiskEventCreateRequest(
                    worker.getId(),
                    RiskSourceType.SOS,
                    RiskType.SOS_REQUEST,
                    RiskLevel.LV4,
                    "SOS 버튼이 눌렸습니다.",
                    request.latitude(),
                    request.longitude(),
                    LocalDateTime.now()
            ));
        } else if (request.riskLevel() != null && request.riskLevel().ordinal() >= RiskLevel.LV3.ordinal() && worker != null) {
            Alert alert = Alert.builder()
                    .riskEvent(null)
                    .worker(worker)
                    .title("위험 알림")
                    .message("센서 기반 위험 단계가 감지되었습니다.")
                    .severity(mapSeverity(request.riskLevel()))
                    .readStatus(AlertReadStatus.UNREAD)
                    .build();
            alertRepository.save(alert);
            alertRealtimeService.publish(AlertConverter.toResponse(alert));
            worker.update(
                    worker.getName(),
                    worker.getDepartment(),
                    worker.getPhoneNumber(),
                    worker.getRfidTag(),
                    request.riskLevel() == RiskLevel.LV4 ? WorkerStatus.DANGER : WorkerStatus.WARNING,
                    worker.getCurrentLatitude(),
                    worker.getCurrentLongitude()
            );
        }

        return SensorLogConverter.toResponse(saved);
    }

    @Override
    public List<SensorLogResponse> findAll() {
        return SensorLogConverter.toResponseList(sensorLogRepository.findAllByOrderByMeasuredAtDesc());
    }

    @Override
    public List<SensorLogResponse> findByWorkerId(Long workerId) {
        return SensorLogConverter.toResponseList(sensorLogRepository.findByWorker_IdOrderByMeasuredAtDesc(workerId));
    }

    @Override
    public List<SensorLogResponse> findByEquipmentId(Long equipmentId) {
        return SensorLogConverter.toResponseList(sensorLogRepository.findByEquipment_IdOrderByMeasuredAtDesc(equipmentId));
    }

    @Override
    public SensorLogResponse findLatestByWorkerId(Long workerId) {
        SensorLog sensorLog = sensorLogRepository.findTopByWorker_IdOrderByMeasuredAtDesc(workerId);
        if (sensorLog == null) {
            throw new BusinessException(ErrorCode.SENSOR_LOG_NOT_FOUND);
        }
        return SensorLogConverter.toResponse(sensorLog);
    }

    private Worker resolveWorker(Long workerId) {
        if (workerId == null) {
            return null;
        }
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private Equipment resolveEquipment(Long equipmentId) {
        if (equipmentId == null) {
            return null;
        }
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    }

    private AlertSeverity mapSeverity(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LV1 -> AlertSeverity.INFO;
            case LV2 -> AlertSeverity.WARNING;
            case LV3 -> AlertSeverity.DANGER;
            case LV4 -> AlertSeverity.EMERGENCY;
        };
    }
}
