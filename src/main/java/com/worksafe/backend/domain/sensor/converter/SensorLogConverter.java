package com.worksafe.backend.domain.sensor.converter;

import com.worksafe.backend.domain.equipment.converter.EquipmentConverter;
import com.worksafe.backend.domain.sensor.dto.request.SensorLogCreateRequest;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.equipment.entity.Equipment;

import java.util.List;

public final class SensorLogConverter {

    private SensorLogConverter() {
    }

    public static SensorLog toEntity(SensorLogCreateRequest request, Worker worker, Equipment equipment) {
        return SensorLog.builder()
                .worker(worker)
                .equipment(equipment)
                .sensorType(request.sensorType())
                .bpm(request.bpm())
                .spo2(request.spo2())
                .bodyTemperature(request.bodyTemperature())
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
                .latitude(request.latitude())
                .longitude(request.longitude())
                .speed(request.speed())
                .pressureValue(request.pressureValue())
                .lidarFrontLeft(request.lidarFrontLeft())
                .lidarFrontRight(request.lidarFrontRight())
                .lidarBackLeft(request.lidarBackLeft())
                .lidarBackRight(request.lidarBackRight())
                .lidarSideLeft(request.lidarSideLeft())
                .lidarSideRight(request.lidarSideRight())
                .ultrasonicDistance(request.ultrasonicDistance())
                .rawPayload(request.rawPayload())
                .wearStatus(request.wearStatus())
                .sosPressed(Boolean.TRUE.equals(request.sosPressed()))
                .riskLevel(request.riskLevel())
                .measuredAt(request.measuredAt())
                .build();
    }

    public static SensorLogResponse toResponse(SensorLog sensorLog) {
        return new SensorLogResponse(
                sensorLog.getId(),
                sensorLog.getWorker() == null ? null : WorkerConverter.toResponse(sensorLog.getWorker()),
                sensorLog.getEquipment() == null ? null : EquipmentConverter.toResponse(sensorLog.getEquipment()),
                sensorLog.getSensorType(),
                sensorLog.getBpm(),
                sensorLog.getSpo2(),
                sensorLog.getBodyTemperature(),
                sensorLog.getAccelerationX(),
                sensorLog.getAccelerationY(),
                sensorLog.getAccelerationZ(),
                sensorLog.getGyroX(),
                sensorLog.getGyroY(),
                sensorLog.getGyroZ(),
                sensorLog.getTiltX(),
                sensorLog.getTiltY(),
                sensorLog.getTiltZ(),
                sensorLog.getImpactAmount(),
                sensorLog.getLatitude(),
                sensorLog.getLongitude(),
                sensorLog.getSpeed(),
                sensorLog.getPressureValue(),
                sensorLog.getLidarFrontLeft(),
                sensorLog.getLidarFrontRight(),
                sensorLog.getLidarBackLeft(),
                sensorLog.getLidarBackRight(),
                sensorLog.getLidarSideLeft(),
                sensorLog.getLidarSideRight(),
                sensorLog.getUltrasonicDistance(),
                sensorLog.getRawPayload(),
                sensorLog.getWearStatus(),
                sensorLog.isSosPressed(),
                sensorLog.getRiskLevel(),
                sensorLog.getMeasuredAt(),
                sensorLog.getCreatedAt()
        );
    }

    public static List<SensorLogResponse> toResponseList(List<SensorLog> sensorLogs) {
        return sensorLogs.stream().map(SensorLogConverter::toResponse).toList();
    }
}
