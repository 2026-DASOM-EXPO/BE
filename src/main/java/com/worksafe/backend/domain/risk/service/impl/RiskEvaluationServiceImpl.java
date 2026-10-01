package com.worksafe.backend.domain.risk.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RiskEvaluationServiceImpl implements RiskEvaluationService {

    private static final List<RiskStatus> ACTIVE_STATUSES = List.of(RiskStatus.OPEN, RiskStatus.PROCESSING);

    private final RiskEventRepository riskEventRepository;
    private final SensorLogRepository sensorLogRepository;
    private final WorkerRepository workerRepository;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;

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

        for (RiskEvent activeRiskEvent : riskEventRepository.findByWorker_IdAndStatusInOrderByOccurredAtDesc(workerId, ACTIVE_STATUSES)) {
            if (activeRiskEvent.getSourceType() != RiskSourceType.SENSOR) {
                riskLevel = max(riskLevel, activeRiskEvent.getRiskLevel());
            }
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
        sensorLog.applyAssessment(sensorLog.getWearStatus(), riskLevel);
        if (riskLevel == RiskLevel.LV1 || riskType == null) {
            if (riskType != null) {
                resolveSensorRiskIfRecovered(worker, riskType);
            }
            evaluateWorkerRisk(worker.getId());
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
                    sensorLog.getLongitude()
            );
            log.warn(
                    "RISK_ACTIVE workerId={} eventId={} type={} currentLevel={} peakLevel={} detectedAt={}",
                    worker.getId(),
                    existing.getId(),
                    riskType,
                    riskLevel,
                    existing.getRiskLevel(),
                    existing.getOccurredAt()
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
        log.warn(
                "RISK_DETECTED workerId={} eventId={} type={} level={} detectedAt={}",
                worker.getId(),
                saved.getId(),
                riskType,
                riskLevel,
                saved.getOccurredAt()
        );
        handleRiskEvent(saved);
        return RiskEventConverter.toResponse(saved);
    }

    @Override
    public RiskEventResponse evaluateByEquipmentStatus(Long workerId) {
        evaluateWorkerRisk(workerId);
        return null;
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

        updateWorkerStatus(riskEvent.getWorker(), riskEvent.getRiskLevel());
    }

    private RiskLevel determineRiskLevel(SensorLog sensorLog) {
        return switch (sensorLog.getSensorType()) {
            case BIOMETRIC -> evaluateBiometricRiskLevel(sensorLog);
            case MOTION -> evaluateMotionRiskLevel(sensorLog);
            case WEAR_STATUS -> RiskLevel.LV1;
            case SOS -> RiskLevel.LV3;
            default -> RiskLevel.LV1;
        };
    }

    private RiskType determineRiskType(SensorLog sensorLog) {
        return switch (sensorLog.getSensorType()) {
            case BIOMETRIC -> RiskType.BIOMETRIC_ABNORMAL;
            case MOTION -> RiskType.FALL_DETECTED;
            case WEAR_STATUS -> null;
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
        double accelerationMagnitude = vectorMagnitude(sensorLog.getAccelX(), sensorLog.getAccelY(), sensorLog.getAccelZ());
        double maxTilt = maxAbs(sensorLog.getTiltX(), sensorLog.getTiltY(), sensorLog.getTiltZ());
        double impactAmount = sensorLog.getImpactAmount() == null ? 0.0 : sensorLog.getImpactAmount();
        double angularVelocity = vectorMagnitude(sensorLog.getGyroX(), sensorLog.getGyroY(), sensorLog.getGyroZ());

        if (accelerationMagnitude >= 2.5 || maxTilt >= 60.0 || impactAmount >= 3.0 || angularVelocity >= 8.0) {
            return RiskLevel.LV4;
        }
        if (accelerationMagnitude >= 2.0 || maxTilt >= 45.0 || impactAmount >= 1.5 || angularVelocity >= 5.0) {
            return RiskLevel.LV3;
        }
        if (accelerationMagnitude >= 1.5 || maxTilt >= 30.0 || impactAmount >= 1.0 || angularVelocity >= 3.0) {
            return RiskLevel.LV2;
        }
        return RiskLevel.LV1;
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
            Double longitude
    ) {
        RiskLevel nextRiskLevel = max(riskEvent.getRiskLevel(), riskLevel);
        riskEvent.updateCurrentRisk(
                sourceType,
                nextRiskLevel,
                description,
                latitude,
                longitude,
                riskEvent.getOccurredAt()
        );
    }

    private void resolveSensorRiskIfRecovered(Worker worker, RiskType riskType) {
        RiskEvent activeRisk = findActiveRiskEvent(worker.getId(), riskType);
        if (activeRisk != null && activeRisk.getSourceType() == RiskSourceType.SENSOR) {
            activeRisk.changeStatus(RiskStatus.RESOLVED);
            log.info(
                    "RISK_RESOLVED workerId={} eventId={} type={} peakLevel={} detectedAt={} resolvedAt={}",
                    worker.getId(),
                    activeRisk.getId(),
                    riskType,
                    activeRisk.getRiskLevel(),
                    activeRisk.getOccurredAt(),
                    activeRisk.getResolvedAt()
            );
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
