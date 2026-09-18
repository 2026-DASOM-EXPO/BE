package com.worksafe.backend.domain.sensor.entity;

import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.global.common.BaseEntity;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import com.worksafe.backend.domain.worker.entity.Worker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "sensor_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SensorType sensorType;

    private Integer bpm;
    private Double spo2;
    private Double bodyTemperature;
    @Column(name = "accelerationx")
    private Double accelX;
    @Column(name = "accelerationy")
    private Double accelY;
    @Column(name = "accelerationz")
    private Double accelZ;
    private Double gyroX;
    private Double gyroY;
    private Double gyroZ;
    private Double tiltX;
    private Double tiltY;
    private Double tiltZ;
    private Double impactAmount;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Integer pressureValue;
    private Double lidarFrontLeft;
    private Double lidarFrontRight;
    private Double lidarBackLeft;
    private Double lidarBackRight;
    private Double lidarSideLeft;
    private Double lidarSideRight;
    private Double ultrasonicDistance;
    @Column(length = 2000)
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WearStatus wearStatus;

    @Column(nullable = false)
    private boolean sosPressed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RiskLevel riskLevel;

    private LocalDateTime measuredAt;

    @Builder
    private SensorLog(
            Worker worker,
            Equipment equipment,
            SensorType sensorType,
            Integer bpm,
            Double spo2,
            Double bodyTemperature,
            Double accelX,
            Double accelY,
            Double accelZ,
            Double gyroX,
            Double gyroY,
            Double gyroZ,
            Double tiltX,
            Double tiltY,
            Double tiltZ,
            Double impactAmount,
            Double latitude,
            Double longitude,
            Double speed,
            Integer pressureValue,
            Double lidarFrontLeft,
            Double lidarFrontRight,
            Double lidarBackLeft,
            Double lidarBackRight,
            Double lidarSideLeft,
            Double lidarSideRight,
            Double ultrasonicDistance,
            String rawPayload,
            WearStatus wearStatus,
            boolean sosPressed,
            RiskLevel riskLevel,
            LocalDateTime measuredAt
    ) {
        this.worker = worker;
        this.equipment = equipment;
        this.sensorType = sensorType;
        this.bpm = bpm;
        this.spo2 = spo2;
        this.bodyTemperature = bodyTemperature;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
        this.tiltX = tiltX;
        this.tiltY = tiltY;
        this.tiltZ = tiltZ;
        this.impactAmount = impactAmount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.pressureValue = pressureValue;
        this.lidarFrontLeft = lidarFrontLeft;
        this.lidarFrontRight = lidarFrontRight;
        this.lidarBackLeft = lidarBackLeft;
        this.lidarBackRight = lidarBackRight;
        this.lidarSideLeft = lidarSideLeft;
        this.lidarSideRight = lidarSideRight;
        this.ultrasonicDistance = ultrasonicDistance;
        this.rawPayload = rawPayload;
        this.wearStatus = wearStatus;
        this.sosPressed = sosPressed;
        this.riskLevel = riskLevel;
        this.measuredAt = measuredAt;
    }

    public void applyAssessment(WearStatus wearStatus, RiskLevel riskLevel) {
        this.wearStatus = wearStatus;
        this.riskLevel = riskLevel;
    }

    public void updateCurrentState(SensorLog sensorLog) {
        this.worker = sensorLog.worker;
        this.equipment = sensorLog.equipment;
        this.sensorType = sensorLog.sensorType;
        this.bpm = sensorLog.bpm;
        this.spo2 = sensorLog.spo2;
        this.bodyTemperature = sensorLog.bodyTemperature;
        this.accelX = sensorLog.accelX;
        this.accelY = sensorLog.accelY;
        this.accelZ = sensorLog.accelZ;
        this.gyroX = sensorLog.gyroX;
        this.gyroY = sensorLog.gyroY;
        this.gyroZ = sensorLog.gyroZ;
        this.tiltX = sensorLog.tiltX;
        this.tiltY = sensorLog.tiltY;
        this.tiltZ = sensorLog.tiltZ;
        this.impactAmount = sensorLog.impactAmount;
        this.latitude = sensorLog.latitude;
        this.longitude = sensorLog.longitude;
        this.speed = sensorLog.speed;
        this.pressureValue = sensorLog.pressureValue;
        this.lidarFrontLeft = sensorLog.lidarFrontLeft;
        this.lidarFrontRight = sensorLog.lidarFrontRight;
        this.lidarBackLeft = sensorLog.lidarBackLeft;
        this.lidarBackRight = sensorLog.lidarBackRight;
        this.lidarSideLeft = sensorLog.lidarSideLeft;
        this.lidarSideRight = sensorLog.lidarSideRight;
        this.ultrasonicDistance = sensorLog.ultrasonicDistance;
        this.rawPayload = sensorLog.rawPayload;
        this.wearStatus = sensorLog.wearStatus;
        this.sosPressed = sensorLog.sosPressed;
        this.riskLevel = sensorLog.riskLevel;
        this.measuredAt = sensorLog.measuredAt;
    }
}
