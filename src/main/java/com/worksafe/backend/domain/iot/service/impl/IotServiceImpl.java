package com.worksafe.backend.domain.iot.service.impl;

import com.worksafe.backend.alert.entity.Alert;
import com.worksafe.backend.alert.enums.AlertReadStatus;
import com.worksafe.backend.alert.enums.AlertSeverity;
import com.worksafe.backend.alert.repository.AlertRepository;
import com.worksafe.backend.alert.service.AlertRealtimeService;
import com.worksafe.backend.drone.entity.DroneDispatch;
import com.worksafe.backend.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.drone.enums.DroneStatus;
import com.worksafe.backend.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.drone.repository.DroneRepository;
import com.worksafe.backend.equipment.entity.Equipment;
import com.worksafe.backend.equipment.enums.AttendanceType;
import com.worksafe.backend.equipment.enums.WearStatus;
import com.worksafe.backend.equipment.repository.EquipmentRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.iot.dto.request.AttendanceRequest;
import com.worksafe.backend.iot.dto.request.BiometricRequest;
import com.worksafe.backend.iot.dto.request.DroneObstacleRequest;
import com.worksafe.backend.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.iot.dto.request.GpsRequest;
import com.worksafe.backend.iot.dto.request.ImuRequest;
import com.worksafe.backend.iot.dto.request.SosRequest;
import com.worksafe.backend.iot.dto.response.AttendanceResponse;
import com.worksafe.backend.iot.service.IotService;
import com.worksafe.backend.risk.converter.RiskEventConverter;
import com.worksafe.backend.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.risk.entity.RiskEvent;
import com.worksafe.backend.risk.enums.RiskLevel;
import com.worksafe.backend.risk.enums.RiskSourceType;
import com.worksafe.backend.risk.enums.RiskStatus;
import com.worksafe.backend.risk.enums.RiskType;
import com.worksafe.backend.risk.repository.RiskEventRepository;
import com.worksafe.backend.risk.service.RiskEvaluationService;
import com.worksafe.backend.risk.service.RiskService;
import com.worksafe.backend.sensor.converter.SensorLogConverter;
import com.worksafe.backend.sensor.dto.request.SensorLogCreateRequest;
import com.worksafe.backend.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.sensor.entity.SensorLog;
import com.worksafe.backend.sensor.enums.SensorType;
import com.worksafe.backend.sensor.repository.SensorLogRepository;
import com.worksafe.backend.worker.entity.Worker;
import com.worksafe.backend.worker.enums.WorkerStatus;
import com.worksafe.backend.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IotServiceImpl implements IotService {

    private static final List<RiskStatus> ACTIVE_RISK_STATUSES = List.of(RiskStatus.OPEN, RiskStatus.PROCESSING);

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

    @Override
    public AttendanceResponse attendance(AttendanceRequest request) {
        Worker worker = getWorkerByRfid(request.rfidTag());
        AttendanceType attendanceType = request.attendanceType();

        if (attendanceType == AttendanceType.CHECK_IN && worker.getStatus() != WorkerStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }
        if (attendanceType == AttendanceType.CHECK_OUT && worker.getStatus() == WorkerStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        worker.update(
                worker.getName(),
                worker.getDepartment(),
                worker.getPhoneNumber(),
                worker.getRfidTag(),
                attendanceType == AttendanceType.CHECK_IN ? WorkerStatus.NORMAL : WorkerStatus.INACTIVE,
                worker.getCurrentLatitude(),
                worker.getCurrentLongitude()
        );

        sensorLogRepository.save(SensorLog.builder()
                .worker(worker)
                .sensorType(SensorType.RFID)
                .rawPayload(request.rfidTag())
                .sosPressed(false)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build());

        return new AttendanceResponse(worker.getId(), worker.getName(), worker.getStatus());
    }

    @Override
    public SensorLogResponse biometrics(BiometricRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        SensorLog saved = sensorLogRepository.save(buildSensorLog(
                worker,
                equipment,
                request
        ));

        riskEvaluationService.evaluateBySensorLog(saved);
        riskEvaluationService.evaluateWorkerRisk(worker.getId());
        return SensorLogConverter.toResponse(saved);
    }

    @Override
    public SensorLogResponse imu(ImuRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        SensorLog saved = sensorLogRepository.save(buildSensorLog(
                worker,
                equipment,
                request
        ));

        riskEvaluationService.evaluateBySensorLog(saved);
        riskEvaluationService.evaluateWorkerRisk(worker.getId());
        return SensorLogConverter.toResponse(saved);
    }

    @Override
    public SensorLogResponse gps(GpsRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        SensorLog saved = sensorLogRepository.save(buildSensorLog(
                worker,
                equipment,
                request
        ));

        worker.updateLocation(request.latitude(), request.longitude());
        return SensorLogConverter.toResponse(saved);
    }

    @Override
    public SensorLogResponse equipmentStatus(EquipmentStatusRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipment(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        if (equipment.getWearStatus() == request.wearStatus()) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION);
        }

        equipment.updateWearStatus(request.wearStatus(), request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt());

        SensorLog saved = sensorLogRepository.save(buildSensorLog(
                worker,
                equipment,
                request
        ));

        riskEvaluationService.evaluateByEquipmentStatus(worker.getId());
        riskEvaluationService.evaluateWorkerRisk(worker.getId());
        return SensorLogConverter.toResponse(saved);
    }

    @Override
    public RiskEventResponse sos(SosRequest request) {
        Worker worker = getWorker(request.workerId());
        Equipment equipment = getEquipmentIfPresent(request.equipmentId());
        ensureEquipmentMatchesWorker(worker, equipment);

        if (riskEventRepository.existsByWorker_IdAndRiskTypeAndStatusIn(worker.getId(), RiskType.SOS_REQUEST, ACTIVE_RISK_STATUSES)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SOS_REQUEST);
        }

        sensorLogRepository.save(buildSensorLog(
                worker,
                equipment,
                request
        ));

        return riskService.create(new RiskEventCreateRequest(
                worker.getId(),
                RiskSourceType.SOS,
                RiskType.SOS_REQUEST,
                RiskLevel.LV4,
                request.message(),
                request.latitude(),
                request.longitude(),
                request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt()
        ));
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
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build());

        if (Boolean.TRUE.equals(request.obstacleDetected())) {
            DroneDispatch dispatch = getDispatch(request.dispatchId());
            dispatch.changeStatus(DroneDispatchStatus.FAILED);
            dispatch.getDrone().changeStatus(DroneStatus.READY);
            Alert alert = alertRepository.save(Alert.builder()
                    .riskEvent(dispatch.getRiskEvent())
                    .worker(dispatch.getRiskEvent() == null ? null : dispatch.getRiskEvent().getWorker())
                    .title("Drone obstacle detected")
                    .message("Obstacle detected during drone operation.")
                    .severity(AlertSeverity.WARNING)
                    .readStatus(AlertReadStatus.UNREAD)
                    .build());
            alertRealtimeService.publish(com.worksafe.backend.alert.converter.AlertConverter.toResponse(alert));
        }

        return SensorLogConverter.toResponse(saved);
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }

    private Worker getWorkerByRfid(String rfidTag) {
        return workerRepository.findByRfidTag(rfidTag)
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

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, BiometricRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.BIOMETRIC)
                .bpm(request.bpm())
                .spo2(request.spo2())
                .bodyTemperature(request.bodyTemperature())
                .sosPressed(false)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, ImuRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.MOTION)
                .accelerationX(request.accelerationX())
                .accelerationY(request.accelerationY())
                .accelerationZ(request.accelerationZ())
                .gyroX(request.gyroX())
                .gyroY(request.gyroY())
                .gyroZ(request.gyroZ())
                .tiltX(request.tiltX())
                .tiltY(request.tiltY())
                .tiltZ(request.tiltZ())
                .impactAmount(request.impactAmount())
                .sosPressed(false)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, GpsRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.GPS)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .speed(request.speed())
                .sosPressed(false)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build();
    }

    private SensorLog buildSensorLog(Worker worker, Equipment equipment, EquipmentStatusRequest request) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(SensorType.WEAR_STATUS)
                .pressureValue(request.pressureValue())
                .sosPressed(false)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
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
                .sosPressed(true)
                .measuredAt(request.measuredAt() == null ? LocalDateTime.now() : request.measuredAt())
                .build();
    }
}
