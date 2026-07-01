package com.worksafe.backend.domain.sensor.dto.response;

import com.worksafe.backend.domain.equipment.dto.response.EquipmentResponse;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.time.LocalDateTime;

public record SensorLogResponse(
        Long id,
        WorkerResponse worker,
        EquipmentResponse equipment,
        SensorType sensorType,
        Integer bpm,
        Double spo2,
        Double bodyTemperature,
        Double accelerationX,
        Double accelerationY,
        Double accelerationZ,
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
        Double pressureValue,
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
        LocalDateTime measuredAt,
        LocalDateTime createdAt
) {
}
