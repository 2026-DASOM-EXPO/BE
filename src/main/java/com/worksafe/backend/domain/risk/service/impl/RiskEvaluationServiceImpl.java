package com.worksafe.backend.domain.risk.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.risk.service.RiskEvaluationService;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import com.worksafe.backend.domain.sensor.repository.SensorLogRepository;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskEvaluationServiceImpl implements RiskEvaluationService {

    private static final List<RiskStatus> ACTIVE_STATUSES = List.of(RiskStatus.OPEN, RiskStatus.PROCESSING);
    private static final Set<EquipmentType> REQUIRED_EQUIPMENT_TYPES =
            Set.of(EquipmentType.HELMET, EquipmentType.VEST);
    // 경계값이 아닌 뚜렷한 이상값만 위험 판정에 사용합니다.
    private static final int ABNORMAL_HEART_RATE_MIN = 50;
    private static final int ABNORMAL_HEART_RATE_MAX = 120;
    private static final double GYRO_ABNORMAL_THRESHOLD = 3.0;

    private final RiskEventRepository riskEventRepository;
    private final SensorLogRepository sensorLogRepository;
    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;
    private final WearableCommandRepository wearableCommandRepository;

    @Override
    public RiskLevel evaluateWorkerRisk(Long workerId) {
        Worker worker = getWorker(workerId);
        SensorLog biometricLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.BIOMETRIC);
        SensorLog motionLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.MOTION);
        SensorLog sosLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.SOS);

        boolean bothEquipmentNotWorn = areBothRequiredEquipmentNotWorn(workerId);
        boolean heartRateAbnormal = biometricLog != null && isHeartRateAbnormal(biometricLog);
        boolean gyroAbnormal = motionLog != null && isGyroAbnormal(motionLog);
        boolean sosPressed = sosLog != null && sosLog.isSosPressed();

        // LV3: (안전모 미착용 AND 안전조끼 미착용 AND 심박수 이상 AND 자이로 이상) OR SOS
        // LV2: 안전모 미착용 AND 안전조끼 미착용 AND (심박수 이상 OR 자이로 이상)
        RiskLevel riskLevel = sosPressed || (bothEquipmentNotWorn && heartRateAbnormal && gyroAbnormal)
                ? RiskLevel.LV3
                : bothEquipmentNotWorn && (heartRateAbnormal || gyroAbnormal)
                ? RiskLevel.LV2
                : RiskLevel.LV1;

        if (riskLevel == RiskLevel.LV1) {
            resolveEquipmentRiskIfRecovered(worker);
        }

        updateWorkerStatus(worker, riskLevel);
        return riskLevel;
    }

    @Override
    public RiskEventResponse evaluateBySensorLog(SensorLog sensorLog) {
        Worker worker = sensorLog.getWorker();
        if (worker == null) {
            return null;
        }

        RiskType riskType = determineRiskType(sensorLog);
        if (riskType == null || !isAbnormalRiskSensor(sensorLog)) {
            evaluateWorkerRisk(worker.getId());
            return null;
        }
        RiskLevel riskLevel = evaluateWorkerRisk(worker.getId());
        if (riskLevel == RiskLevel.LV1) {
            return null;
        }

        RiskEvent existing = findActiveRiskEvent(worker.getId(), riskType);
        if (existing != null) {
            updateActiveRiskEvent(
                    existing,
                    RiskSourceType.SENSOR,
                    riskLevel,
                    buildDescription(sensorLog, riskType, riskLevel),
                    sensorLog.getLatitude(),
                    sensorLog.getLongitude(),
                    sensorLog.getMeasuredAt() == null ? LocalDateTime.now() : sensorLog.getMeasuredAt()
            );
            updateWorkerStatus(worker, existing.getRiskLevel());
            return RiskEventConverter.toResponse(existing);
        }

        RiskEvent saved = riskEventRepository.save(RiskEvent.builder()
                .worker(worker)
                .sourceType(RiskSourceType.SENSOR)
                .riskType(riskType)
                .riskLevel(riskLevel)
                .description(buildDescription(sensorLog, riskType, riskLevel))
                .latitude(sensorLog.getLatitude())
                .longitude(sensorLog.getLongitude())
                .status(RiskStatus.OPEN)
                .occurredAt(sensorLog.getMeasuredAt() == null ? LocalDateTime.now() : sensorLog.getMeasuredAt())
                .build());
        handleRiskEvent(saved);
        return RiskEventConverter.toResponse(saved);
    }

    @Override
    public RiskEventResponse evaluateByEquipmentStatus(Long workerId) {
        Worker worker = getWorker(workerId);
        RiskLevel riskLevel = evaluateWorkerRisk(workerId);
        if (riskLevel == RiskLevel.LV1) {
            resolveEquipmentRiskIfRecovered(worker);
            return null;
        }

        // 장비 미착용만으로는 위험 이벤트를 만들지 않습니다.
        // 현재 LV2/LV3가 SOS 때문이라면 장비 이벤트로 중복 기록하지 않습니다.
        if (!areBothRequiredEquipmentNotWorn(workerId) || isCurrentSosPressed(workerId)) {
            return null;
        }

        RiskType riskType = RiskType.NO_EQUIPMENT;
        RiskEvent existing = findActiveRiskEvent(workerId, riskType);
        if (existing != null) {
            updateActiveRiskEvent(
                    existing,
                    RiskSourceType.SENSOR,
                    riskLevel,
                    "Safety equipment is not fully worn.",
                    existing.getLatitude(),
                    existing.getLongitude(),
                    LocalDateTime.now()
            );
            updateWorkerStatus(worker, existing.getRiskLevel());
            return RiskEventConverter.toResponse(existing);
        }

        RiskEvent saved = riskEventRepository.save(RiskEvent.builder()
                .worker(worker)
                .sourceType(RiskSourceType.SENSOR)
                .riskType(riskType)
                .riskLevel(riskLevel)
                .description("Safety equipment is not fully worn.")
                .status(RiskStatus.OPEN)
                .occurredAt(LocalDateTime.now())
                .build());
        handleRiskEvent(saved);
        return RiskEventConverter.toResponse(saved);
    }

    @Override
    public RiskEventResponse evaluateBySos(Long workerId) {
        Worker worker = getWorker(workerId);
        RiskEvent existing = findActiveRiskEvent(workerId, RiskType.SOS_REQUEST);
        if (existing != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_SOS_REQUEST);
        }

        RiskEvent saved = riskEventRepository.save(RiskEvent.builder()
                .worker(worker)
                .sourceType(RiskSourceType.SOS)
                .riskType(RiskType.SOS_REQUEST)
                .riskLevel(RiskLevel.LV3)
                .description("SOS request detected.")
                .latitude(worker.getCurrentLatitude())
                .longitude(worker.getCurrentLongitude())
                .status(RiskStatus.OPEN)
                .occurredAt(LocalDateTime.now())
                .build());
        handleRiskEvent(saved);
        return RiskEventConverter.toResponse(saved);
    }

    @Override
    public void handleRiskEvent(RiskEvent riskEvent) {
        createAlertIfNeeded(riskEvent);
        if (riskEvent.getRiskType() == RiskType.NO_EQUIPMENT) {
            createBuzzerCommandIfNeeded(riskEvent);
        }
        updateWorkerStatus(riskEvent.getWorker(), riskEvent.getRiskLevel());
    }

    private RiskType determineRiskType(SensorLog sensorLog) {
        return switch (sensorLog.getSensorType()) {
            case BIOMETRIC -> RiskType.BIOMETRIC_ABNORMAL;
            case MOTION -> RiskType.FALL_DETECTED;
            case WEAR_STATUS -> sensorLog.getWearStatus() == WearStatus.NOT_WORN ? RiskType.NO_EQUIPMENT : null;
            case SOS -> RiskType.SOS_REQUEST;
            default -> null;
        };
    }

    private boolean isAbnormalRiskSensor(SensorLog sensorLog) {
        return switch (sensorLog.getSensorType()) {
            case BIOMETRIC -> isHeartRateAbnormal(sensorLog);
            case MOTION -> isGyroAbnormal(sensorLog);
            case SOS -> sensorLog.isSosPressed();
            default -> false;
        };
    }

    private boolean areBothRequiredEquipmentNotWorn(Long workerId) {
        List<Equipment> equipmentList = equipmentRepository.findByWorker_IdOrderByUpdatedAtDesc(workerId);
        return REQUIRED_EQUIPMENT_TYPES.stream().allMatch(requiredType ->
                latestEquipment(equipmentList, requiredType)
                        .map(equipment -> equipment.getWearStatus() == WearStatus.NOT_WORN)
                        .orElse(false));
    }

    private boolean isCurrentSosPressed(Long workerId) {
        SensorLog sosLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.SOS);
        return sosLog != null && sosLog.isSosPressed();
    }

    private Optional<Equipment> latestEquipment(List<Equipment> equipmentList, EquipmentType equipmentType) {
        return equipmentList.stream()
                .filter(equipment -> equipment.getType() == equipmentType)
                .findFirst();
    }

    private boolean isHeartRateAbnormal(SensorLog sensorLog) {
        Integer bpm = sensorLog.getBpm();
        return bpm != null && (bpm < ABNORMAL_HEART_RATE_MIN || bpm > ABNORMAL_HEART_RATE_MAX);
    }

    private boolean isGyroAbnormal(SensorLog sensorLog) {
        return maxAbs(sensorLog.getGyroX(), sensorLog.getGyroY(), sensorLog.getGyroZ()) >= GYRO_ABNORMAL_THRESHOLD;
    }

    private void createAlertIfNeeded(RiskEvent riskEvent) {
        if (riskEvent.getRiskLevel().ordinal() < RiskLevel.LV2.ordinal()) {
            return;
        }
        if (riskEvent.getId() != null && alertRepository.existsByRiskEvent_Id(riskEvent.getId())) {
            return;
        }

        Alert alert = alertRepository.save(Alert.builder()
                .riskEvent(riskEvent)
                .worker(riskEvent.getWorker())
                .title(alertTitle(riskEvent))
                .message(riskEvent.getDescription())
                .severity(mapSeverity(riskEvent.getRiskLevel()))
                .readStatus(AlertReadStatus.UNREAD)
                .build());
        alertRealtimeService.publish(AlertConverter.toResponse(alert));
    }

    private void updateActiveRiskEvent(
            RiskEvent riskEvent,
            RiskSourceType sourceType,
            RiskLevel riskLevel,
            String description,
            Double latitude,
            Double longitude,
            LocalDateTime occurredAt
    ) {
        RiskLevel nextRiskLevel = max(riskEvent.getRiskLevel(), riskLevel);
        riskEvent.updateCurrentRisk(
                sourceType,
                nextRiskLevel,
                description,
                latitude,
                longitude,
                occurredAt
        );
    }

    private void createBuzzerCommandIfNeeded(RiskEvent riskEvent) {
        if (riskEvent.getRiskLevel().ordinal() < RiskLevel.LV2.ordinal() || riskEvent.getWorker() == null) {
            return;
        }

        Equipment equipment = equipmentRepository
                .findFirstByWorker_IdAndTypeOrderByUpdatedAtDesc(riskEvent.getWorker().getId(), EquipmentType.VEST)
                .orElseGet(() -> equipmentRepository
                        .findFirstByWorker_IdAndTypeOrderByUpdatedAtDesc(riskEvent.getWorker().getId(), EquipmentType.SOS_BUTTON)
                        .orElse(null));
        if (equipment == null) {
            return;
        }

        wearableCommandRepository.save(WearableCommand.builder()
                .equipment(equipment)
                .worker(riskEvent.getWorker())
                .commandType(WearableCommandType.BUZZER_ON)
                .commandStatus(WearableCommandStatus.REQUESTED)
                .reason(riskEvent.getDescription())
                .requestedAt(LocalDateTime.now())
                .build());
    }

    private void resolveEquipmentRiskIfRecovered(Worker worker) {
        List<RiskEvent> activeEquipmentRisks = riskEventRepository
                .findByWorker_IdAndStatusInOrderByOccurredAtDesc(worker.getId(), ACTIVE_STATUSES)
                .stream()
                .filter(event -> event.getRiskType() == RiskType.NO_EQUIPMENT)
                .toList();
        if (activeEquipmentRisks.isEmpty()) {
            return;
        }
        activeEquipmentRisks.forEach(event -> event.changeStatus(RiskStatus.RESOLVED));

        Equipment vest = equipmentRepository
                .findFirstByWorker_IdAndTypeOrderByUpdatedAtDesc(worker.getId(), EquipmentType.VEST)
                .orElseGet(() -> equipmentRepository
                        .findFirstByWorker_IdAndTypeOrderByUpdatedAtDesc(worker.getId(), EquipmentType.SOS_BUTTON)
                        .orElse(null));
        if (vest != null) {
            wearableCommandRepository.save(WearableCommand.builder()
                    .equipment(vest)
                    .worker(worker)
                    .commandType(WearableCommandType.BUZZER_OFF)
                    .commandStatus(WearableCommandStatus.REQUESTED)
                    .reason("필수 안전장비가 모두 다시 착용되었습니다.")
                    .requestedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void updateWorkerStatus(Worker worker, RiskLevel riskLevel) {
        if (worker == null || worker.getStatus() == WorkerStatus.INACTIVE) {
            return;
        }

        WorkerStatus status = switch (riskLevel) {
            case LV1 -> WorkerStatus.NORMAL;
            case LV2 -> WorkerStatus.WARNING;
            case LV3, LV4 -> WorkerStatus.DANGER;
        };

        worker.update(
                worker.getName(),
                worker.getDepartment(),
                worker.getPhoneNumber(),
                status,
                worker.getCurrentLatitude(),
                worker.getCurrentLongitude()
        );
    }

    private RiskEvent findActiveRiskEvent(Long workerId, RiskType riskType) {
        return riskEventRepository.findFirstByWorker_IdAndRiskTypeAndStatusInOrderByOccurredAtDesc(workerId, riskType, ACTIVE_STATUSES);
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private AlertSeverity mapSeverity(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LV1 -> AlertSeverity.INFO;
            case LV2 -> AlertSeverity.WARNING;
            case LV3, LV4 -> AlertSeverity.DANGER;
        };
    }

    private String alertTitle(RiskEvent riskEvent) {
        return switch (riskEvent.getRiskLevel()) {
            case LV1 -> "정상";
            case LV2 -> "주의";
            case LV3, LV4 -> "위험";
        };
    }

    private RiskLevel max(RiskLevel first, RiskLevel second) {
        RiskLevel normalizedFirst = first == RiskLevel.LV4 ? RiskLevel.LV3 : first;
        RiskLevel normalizedSecond = second == RiskLevel.LV4 ? RiskLevel.LV3 : second;
        return normalizedFirst.ordinal() >= normalizedSecond.ordinal() ? normalizedFirst : normalizedSecond;
    }

    private double maxAbs(Double x, Double y, Double z) {
        return Math.max(Math.max(Math.abs(x == null ? 0.0 : x), Math.abs(y == null ? 0.0 : y)), Math.abs(z == null ? 0.0 : z));
    }

    private String buildDescription(SensorLog sensorLog, RiskType riskType, RiskLevel riskLevel) {
        return switch (riskType) {
            case BIOMETRIC_ABNORMAL -> String.format(Locale.ROOT, "생체 이상이 감지되었습니다. (%s)", riskLevel);
            case FALL_DETECTED -> "모션 센서에서 낙상이 감지되었습니다.";
            case NO_EQUIPMENT -> "안전장비가 착용되지 않았습니다.";
            case SOS_REQUEST -> "SOS 신고가 접수되었습니다.";
            case LOCATION_ABNORMAL -> "위치 이상이 감지되었습니다.";
            case DRONE_DISPATCHED -> "드론이 출동했습니다.";
        };
    }
}
