package com.worksafe.backend.domain.risk.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.EmergencyCallStatus;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
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

@Service
@RequiredArgsConstructor
@Transactional
public class RiskEvaluationServiceImpl implements RiskEvaluationService {

    private static final List<RiskStatus> ACTIVE_STATUSES = List.of(RiskStatus.OPEN, RiskStatus.PROCESSING);

    private final RiskEventRepository riskEventRepository;
    private final SensorLogRepository sensorLogRepository;
    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;
    private final DroneRepository droneRepository;
    private final DroneDispatchRepository droneDispatchRepository;
    private final WearableCommandRepository wearableCommandRepository;

    @Override
    public RiskLevel evaluateWorkerRisk(Long workerId) {
        Worker worker = getWorker(workerId);
        RiskLevel riskLevel = RiskLevel.LV1;

        SensorLog biometricLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.BIOMETRIC);
        if (biometricLog != null) {
            riskLevel = max(riskLevel, evaluateBiometricRiskLevel(biometricLog));
        }

        SensorLog motionLog = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(workerId, SensorType.MOTION);
        if (motionLog != null) {
            riskLevel = max(riskLevel, evaluateMotionRiskLevel(motionLog));
        }

        riskLevel = max(riskLevel, evaluateEquipmentRiskLevel(workerId));
        for (RiskEvent activeRiskEvent : riskEventRepository.findByWorker_IdAndStatusInOrderByOccurredAtDesc(workerId, ACTIVE_STATUSES)) {
            riskLevel = max(riskLevel, activeRiskEvent.getRiskLevel());
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
        RiskLevel riskLevel = determineRiskLevel(sensorLog);
        if (riskLevel == RiskLevel.LV1 || riskType == null) {
            evaluateWorkerRisk(worker.getId());
            return null;
        }

        RiskEvent existing = findActiveRiskEvent(worker.getId(), riskType);
        if (existing != null && existing.getRiskLevel().ordinal() >= riskLevel.ordinal()) {
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
        RiskLevel riskLevel = evaluateEquipmentRiskLevel(workerId);
        if (riskLevel == RiskLevel.LV1) {
            updateWorkerStatus(worker, riskLevel);
            return null;
        }

        RiskType riskType = RiskType.NO_EQUIPMENT;
        RiskEvent existing = findActiveRiskEvent(workerId, riskType);
        if (existing != null && existing.getRiskLevel().ordinal() >= riskLevel.ordinal()) {
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
                .riskLevel(RiskLevel.LV4)
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
        createBuzzerCommandIfNeeded(riskEvent);
        dispatchDroneIfNeeded(riskEvent);
        updateWorkerStatus(riskEvent.getWorker(), riskEvent.getRiskLevel());
    }

    private RiskLevel determineRiskLevel(SensorLog sensorLog) {
        return switch (sensorLog.getSensorType()) {
            case BIOMETRIC -> evaluateBiometricRiskLevel(sensorLog);
            case MOTION -> evaluateMotionRiskLevel(sensorLog);
            case WEAR_STATUS -> sensorLog.getWearStatus() == WearStatus.NOT_WORN ? RiskLevel.LV2 : RiskLevel.LV1;
            case SOS -> RiskLevel.LV4;
            default -> RiskLevel.LV1;
        };
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

    private RiskLevel evaluateBiometricRiskLevel(SensorLog sensorLog) {
        int score = 0;

        if (sensorLog.getBpm() != null) {
            if (sensorLog.getBpm() < 40 || sensorLog.getBpm() > 140) {
                return RiskLevel.LV4;
            }
            if (sensorLog.getBpm() < 50 || sensorLog.getBpm() > 120) {
                score = Math.max(score, 3);
            } else if (sensorLog.getBpm() < 60 || sensorLog.getBpm() > 100) {
                score = Math.max(score, 2);
            }
        }

        if (sensorLog.getSpo2() != null) {
            if (sensorLog.getSpo2() < 88) {
                return RiskLevel.LV4;
            }
            if (sensorLog.getSpo2() < 92) {
                score = Math.max(score, 3);
            } else if (sensorLog.getSpo2() < 95) {
                score = Math.max(score, 2);
            }
        }

        if (sensorLog.getBodyTemperature() != null) {
            if (sensorLog.getBodyTemperature() >= 39.0 || sensorLog.getBodyTemperature() <= 34.0) {
                return RiskLevel.LV4;
            }
            if (sensorLog.getBodyTemperature() >= 37.8 || sensorLog.getBodyTemperature() <= 35.0) {
                score = Math.max(score, 3);
            } else if (sensorLog.getBodyTemperature() >= 37.3) {
                score = Math.max(score, 2);
            }
        }

        return toRiskLevel(score);
    }

    private RiskLevel evaluateMotionRiskLevel(SensorLog sensorLog) {
        double accelerationMagnitude = vectorMagnitude(sensorLog.getAccelerationX(), sensorLog.getAccelerationY(), sensorLog.getAccelerationZ());
        double maxTilt = maxAbs(sensorLog.getTiltX(), sensorLog.getTiltY(), sensorLog.getTiltZ());
        double impactAmount = sensorLog.getImpactAmount() == null ? 0.0 : sensorLog.getImpactAmount();

        if (accelerationMagnitude >= 2.5 || maxTilt >= 60.0 || impactAmount >= 3.0) {
            return RiskLevel.LV4;
        }
        if (accelerationMagnitude >= 2.0 || maxTilt >= 45.0 || impactAmount >= 1.5) {
            return RiskLevel.LV3;
        }
        return RiskLevel.LV1;
    }

    private RiskLevel evaluateEquipmentRiskLevel(Long workerId) {
        List<Equipment> equipmentList = equipmentRepository.findByWorker_IdOrderByUpdatedAtDesc(workerId);
        if (equipmentList.isEmpty()) {
            return RiskLevel.LV1;
        }

        boolean anyNotWorn = equipmentList.stream().anyMatch(equipment -> equipment.getWearStatus() == WearStatus.NOT_WORN);
        boolean allNotWornOrUnknown = equipmentList.stream().allMatch(equipment ->
                equipment.getWearStatus() == WearStatus.NOT_WORN || equipment.getWearStatus() == WearStatus.UNKNOWN);

        if (allNotWornOrUnknown && anyNotWorn) {
            return RiskLevel.LV3;
        }
        if (anyNotWorn) {
            return RiskLevel.LV2;
        }
        return RiskLevel.LV1;
    }

    private void createAlertIfNeeded(RiskEvent riskEvent) {
        if (riskEvent.getRiskLevel().ordinal() < RiskLevel.LV2.ordinal()) {
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

    private void createBuzzerCommandIfNeeded(RiskEvent riskEvent) {
        if (riskEvent.getRiskLevel().ordinal() < RiskLevel.LV2.ordinal() || riskEvent.getWorker() == null) {
            return;
        }

        Equipment equipment = equipmentRepository.findFirstByWorker_IdOrderByUpdatedAtDesc(riskEvent.getWorker().getId()).orElse(null);
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

    private void dispatchDroneIfNeeded(RiskEvent riskEvent) {
        if (riskEvent.getRiskLevel().ordinal() < RiskLevel.LV3.ordinal()) {
            return;
        }

        Drone drone = droneRepository.findFirstByStatus(DroneStatus.READY).orElse(null);
        if (drone == null) {
            Alert alert = alertRepository.save(Alert.builder()
                    .riskEvent(riskEvent)
                    .worker(riskEvent.getWorker())
                    .title("대기 중인 드론 없음")
                    .message("대기 중인 드론이 없어 출동을 건너뜁니다.")
                    .severity(mapSeverity(riskEvent.getRiskLevel()))
                    .readStatus(AlertReadStatus.UNREAD)
                    .build());
            alertRealtimeService.publish(AlertConverter.toResponse(alert));
            return;
        }

        DroneDispatch dispatch = droneDispatchRepository.save(DroneDispatch.builder()
                .drone(drone)
                .riskEvent(riskEvent)
                .targetLatitude(riskEvent.getLatitude())
                .targetLongitude(riskEvent.getLongitude())
                .dispatchReason(dispatchReason(riskEvent))
                .emergencyKitMounted(true)
                .emergencyKitDropped(false)
                .dropMethod(DropMethod.MANUAL)
                .emergencyCallRequested(riskEvent.getRiskLevel() == RiskLevel.LV4 || riskEvent.getSourceType() == RiskSourceType.SOS)
                .emergencyCallStatus(riskEvent.getRiskLevel() == RiskLevel.LV4 || riskEvent.getSourceType() == RiskSourceType.SOS
                        ? EmergencyCallStatus.REQUESTED
                        : EmergencyCallStatus.NOT_REQUESTED)
                .status(DroneDispatchStatus.DISPATCHED)
                .commandMessage(riskEvent.getDescription())
                .dispatchedAt(LocalDateTime.now())
                .build());

        drone.changeStatus(DroneStatus.FLYING);
        if (dispatch.isEmergencyCallRequested()) {
            dispatch.markEmergencyCallRequested();
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
                worker.getRfidTag(),
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
            case LV3 -> AlertSeverity.DANGER;
            case LV4 -> AlertSeverity.EMERGENCY;
        };
    }

    private String alertTitle(RiskEvent riskEvent) {
        return switch (riskEvent.getRiskLevel()) {
            case LV1 -> "정상";
            case LV2 -> "주의";
            case LV3 -> "위험";
            case LV4 -> "긴급";
        };
    }

    private String dispatchReason(RiskEvent riskEvent) {
        if (riskEvent.getSourceType() == RiskSourceType.SOS) {
            return "SOS";
        }
        return switch (riskEvent.getRiskType()) {
            case BIOMETRIC_ABNORMAL -> "생체 이상";
            case FALL_DETECTED -> "낙상 감지";
            case NO_EQUIPMENT -> "안전장비 미착용";
            case SOS_REQUEST -> "SOS";
            case LOCATION_ABNORMAL -> "위치 이상";
            case DRONE_DISPATCHED -> "수동";
        };
    }

    private RiskLevel toRiskLevel(int score) {
        return switch (score) {
            case 3 -> RiskLevel.LV3;
            case 2 -> RiskLevel.LV2;
            default -> RiskLevel.LV1;
        };
    }

    private RiskLevel max(RiskLevel first, RiskLevel second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }

    private double vectorMagnitude(Double x, Double y, Double z) {
        double dx = x == null ? 0.0 : x;
        double dy = y == null ? 0.0 : y;
        double dz = z == null ? 0.0 : z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
