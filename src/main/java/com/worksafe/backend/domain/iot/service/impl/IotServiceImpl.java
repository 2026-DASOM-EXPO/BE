package com.worksafe.backend.domain.iot.service.impl;

import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.converter.DroneConverter;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.converter.EquipmentConverter;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.iot.dto.request.BiometricRequest;
import com.worksafe.backend.domain.iot.dto.request.DroneObstacleRequest;
import com.worksafe.backend.domain.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.domain.iot.dto.request.GpsRequest;
import com.worksafe.backend.domain.iot.dto.request.ImuRequest;
import com.worksafe.backend.domain.iot.dto.request.HeartRequest;
import com.worksafe.backend.domain.iot.dto.request.SosRequest;
import com.worksafe.backend.domain.iot.dto.response.SosResponse;
import com.worksafe.backend.domain.iot.service.IotService;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.risk.service.RiskEvaluationService;
import com.worksafe.backend.domain.risk.service.RiskService;
import com.worksafe.backend.domain.sensor.converter.SensorLogConverter;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import com.worksafe.backend.domain.sensor.repository.SensorLogRepository;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IotServiceImpl implements IotService {

    private static final List<RiskStatus> ACTIVE_RISK_STATUSES = List.of(RiskStatus.OPEN, RiskStatus.PROCESSING);
    private static final double GRAVITY_MS2 = 9.80665;

    private final SensorLogRepository sensorLogRepository;
    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;
    private final RiskService riskService;
    private final RiskEvaluationService riskEvaluationService;
    private final RiskEventRepository riskEventRepository;
    private final DroneDispatchRepository droneDispatchRepository;
    private final DroneRepository droneRepository;
    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;

    @Value("${app.sensor.fsr-worn-threshold:1000}")
    private int fsrWornThreshold;

    @Override
    public SensorLogResponse biometrics(BiometricRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        SensorLog saved = saveOrUpdateCurrentByWorker(
                worker,
                SensorType.BIOMETRIC,
                buildSensorLog(
                        worker,
                        equipment,
                        request
                )
        );

        riskEvaluationService.evaluateBySensorLog(saved);
        riskEvaluationService.evaluateWorkerRisk(worker.getId());
        SensorLogResponse response = SensorLogConverter.toResponse(saved);
        alertRealtimeService.publish("sensor", response);
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));
        return response;
    }

    @Override
    public SensorLogResponse heart(HeartRequest request) {
        Worker worker = getWorker(request.workerId());

        SensorLog saved = saveOrUpdateCurrentByWorker(
                worker,
                SensorType.BIOMETRIC,
                buildSensorLog(
                        worker,
                        null,
                        request
                )
        );

        riskEvaluationService.evaluateBySensorLog(saved);
        RiskLevel riskLevel = riskEvaluationService.evaluateWorkerRisk(worker.getId());
        saved.applyAssessment(null, riskLevel);
        SensorLogResponse response = SensorLogConverter.toResponse(saved);
        alertRealtimeService.publish("sensor", response);
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));
        return response;
    }

    @Override
    public SensorLogResponse imu(ImuRequest request) {
        Worker worker = getWorker(request.workerId());

        SensorLog saved = saveOrUpdateCurrentByWorker(
                worker,
                SensorType.MOTION,
                buildSensorLog(
                        worker,
                        null,
                        request
                )
        );

        riskEvaluationService.evaluateBySensorLog(saved);
        RiskLevel riskLevel = riskEvaluationService.evaluateWorkerRisk(worker.getId());
        saved.applyAssessment(null, riskLevel);
        SensorLogResponse response = SensorLogConverter.toResponse(saved);
        alertRealtimeService.publish("sensor", response);
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));
        return response;
    }

    @Override
    public SensorLogResponse gps(GpsRequest request) {
        Worker worker = getWorker(request.workerId());

        SensorLog saved = saveOrUpdateCurrentByWorker(
                worker,
                SensorType.GPS,
                buildSensorLog(
                        worker,
                        null,
                        request
                )
        );

        worker.updateLocation(request.latitude(), request.longitude());
        SensorLogResponse response = SensorLogConverter.toResponse(saved);
        alertRealtimeService.publish("sensor", response);
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));
        return response;
    }

    @Override
    public SensorLogResponse equipmentStatus(EquipmentStatusRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipment(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);
        if (equipment.getType() != EquipmentType.HELMET && equipment.getType() != EquipmentType.SHOES) {
            throw new BusinessException(ErrorCode.INVALID_SENSOR_EQUIPMENT_TYPE);
        }

        WearStatus detectedWearStatus = request.pressureValue() >= fsrWornThreshold
                ? WearStatus.WORN
                : WearStatus.NOT_WORN;
        equipment.updateWearStatus(detectedWearStatus, LocalDateTime.now());

        SensorLog saved = saveOrUpdateCurrentByEquipment(
                worker,
                equipment,
                SensorType.WEAR_STATUS,
                buildSensorLog(
                        worker,
                        equipment,
                        request,
                        detectedWearStatus
                )
        );

        riskEvaluationService.evaluateByEquipmentStatus(worker.getId());
        RiskLevel riskLevel = riskEvaluationService.evaluateWorkerRisk(worker.getId());
        saved.applyAssessment(detectedWearStatus, riskLevel);
        SensorLogResponse response = SensorLogConverter.toResponse(saved);
        alertRealtimeService.publish("sensor", response);
        alertRealtimeService.publish("equipment", EquipmentConverter.toResponse(equipment));
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));
        return response;
    }

    @Override
    public SosResponse sos(SosRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);
        if (equipment != null
                && equipment.getType() != EquipmentType.VEST
                && equipment.getType() != EquipmentType.SOS_BUTTON) {
            throw new BusinessException(ErrorCode.INVALID_SENSOR_EQUIPMENT_TYPE);
        }

        SensorLog saved = saveOrUpdateCurrentByWorker(
                worker,
                SensorType.SOS,
                buildSensorLog(
                        worker,
                        equipment,
                        request
                )
        );
        alertRealtimeService.publish("sensor", SensorLogConverter.toResponse(saved));
        alertRealtimeService.publish("worker", WorkerConverter.toResponse(worker));

        if (request.buttonValue() == 0) {
            return new SosResponse(0, false, null, null, false, false);
        }

        RiskEvent existing = riskEventRepository.findFirstByWorker_IdAndRiskTypeAndStatusInOrderByOccurredAtDesc(
                worker.getId(),
                RiskType.SOS_REQUEST,
                ACTIVE_RISK_STATUSES
        );
        if (existing != null) {
            DroneDispatch existingDispatch = droneDispatchRepository.findFirstByRiskEvent_IdOrderByCreatedAtDesc(existing.getId());
            DroneDispatchResponse dispatchResponse = existingDispatch == null ? null : DroneConverter.toDispatchResponse(existingDispatch);
            return new SosResponse(
                    1,
                    false,
                    RiskEventConverter.toResponse(existing),
                    dispatchResponse,
                    true,
                    existingDispatch != null && existingDispatch.isEmergencyCallRequested()
            );
        }

        RiskEventResponse riskEvent = riskService.create(new RiskEventCreateRequest(
                worker.getId(),
                RiskSourceType.SOS,
                RiskType.SOS_REQUEST,
                RiskLevel.LV3,
                request.message(),
                request.latitude(),
                request.longitude(),
                LocalDateTime.now()
        ));
        DroneDispatch dispatch = droneDispatchRepository.findFirstByRiskEvent_IdOrderByCreatedAtDesc(riskEvent.id());
        return new SosResponse(
                1,
                true,
                riskEvent,
                dispatch == null ? null : DroneConverter.toDispatchResponse(dispatch),
                true,
                dispatch != null && dispatch.isEmergencyCallRequested()
        );
    }

    @Override
    public SensorLogResponse droneObstacle(DroneObstacleRequest request) {
        droneRepository.findById(request.droneId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));

        SensorLog saved = sensorLogRepository.save(SensorLog.builder()
                .sensorType(SensorType.DRONE_OBSTACLE)
                .lidarFrontLeft(request.lidarFrontLeft())
                .lidarFrontRight(request.lidarFrontRight())
                .lidarBackLeft(request.lidarBackLeft())
                .lidarBackRight(request.lidarBackRight())
                .lidarSideLeft(request.lidarSideLeft())
                .lidarSideRight(request.lidarSideRight())
                .ultrasonicDistance(request.ultrasonicDistance())
                .rawPayload(Boolean.TRUE.equals(request.obstacleDetected()) ? "obstacleDetected=true" : "obstacleDetected=false")
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build());

        if (Boolean.TRUE.equals(request.obstacleDetected())) {
            DroneDispatch dispatch = getDispatch(request.dispatchId());
            dispatch.changeStatus(DroneDispatchStatus.FAILED);
            dispatch.getDrone().changeStatus(DroneStatus.READY);
            Alert alert = alertRepository.save(Alert.builder()
                    .riskEvent(dispatch.getRiskEvent())
                    .worker(dispatch.getRiskEvent() == null ? null : dispatch.getRiskEvent().getWorker())
                    .title("드론 장애물 감지")
                    .message("드론 운용 중 장애물이 감지되었습니다.")
                    .severity(AlertSeverity.WARNING)
                    .readStatus(AlertReadStatus.UNREAD)
                    .build());
            alertRealtimeService.publish(AlertConverter.toResponse(alert));
        }

        return SensorLogConverter.toResponse(saved);
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private Equipment getEquipment(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));
    }

    private Equipment getEquipmentIfPresent(Long equipmentId) {
        if (equipmentId == null) {
            return null;
        }
        return getEquipment(equipmentId);
    }

    private void ensureEquipmentMatchesWorker(Worker worker, Equipment equipment) {
        if (equipment != null && equipment.getWorker() != null && !equipment.getWorker().getId().equals(worker.getId())) {
            throw new BusinessException(ErrorCode.DEVICE_WORKER_MISMATCH);
        }
    }

    private DroneDispatch getDispatch(Long dispatchId) {
        if (dispatchId == null) {
            throw new BusinessException(ErrorCode.DRONE_DISPATCH_NOT_FOUND);
        }
        return droneDispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_DISPATCH_NOT_FOUND));
    }

    private SensorLog saveOrUpdateCurrentByWorker(Worker worker, SensorType sensorType, SensorLog newState) {
        SensorLog current = sensorLogRepository.findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(
                worker.getId(),
                sensorType
        );
        if (current == null) {
            return sensorLogRepository.save(newState);
        }
        current.updateCurrentState(newState);
        return current;
    }

    private SensorLog saveOrUpdateCurrentByEquipment(
            Worker worker,
            Equipment equipment,
            SensorType sensorType,
            SensorLog newState
    ) {
        SensorLog current = sensorLogRepository.findTopByWorker_IdAndSensorTypeAndEquipment_IdOrderByMeasuredAtDesc(
                worker.getId(),
                sensorType,
                equipment.getId()
        );
        if (current == null) {
            return sensorLogRepository.save(newState);
        }
        current.updateCurrentState(newState);
        return current;
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, BiometricRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.BIOMETRIC)
                .bpm(request.bpm())
                .spo2(request.spo2())
                .bodyTemperature(request.bodyTemperature())
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, HeartRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.BIOMETRIC)
                .bpm(request.bpm())
                .rawPayload("source=HEART")
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, ImuRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.MOTION)
                .accelX(toGravityUnit(request.accelX()))
                .accelY(toGravityUnit(request.accelY()))
                .accelZ(toGravityUnit(request.accelZ()))
                .gyroX(request.gyroX())
                .gyroY(request.gyroY())
                .gyroZ(request.gyroZ())
                .rawPayload("accelerationUnit=m/s^2;gyroscopeUnit=rad/s")
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private Double toGravityUnit(Double value) {
        if (value == null) {
            return null;
        }
        return value / GRAVITY_MS2;
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, GpsRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.GPS)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private SensorLog buildSensorLog(
            Worker worker,
            Equipment equipment,
            EquipmentStatusRequest request,
            WearStatus detectedWearStatus
    ) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.WEAR_STATUS)
                .pressureValue(request.pressureValue())
                .wearStatus(detectedWearStatus)
                .sosPressed(false)
                .measuredAt(LocalDateTime.now())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, SosRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.SOS)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .rawPayload(request.message())
                .sosPressed(request.buttonValue() == 1)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}
