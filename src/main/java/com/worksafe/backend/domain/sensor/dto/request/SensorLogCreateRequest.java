package com.worksafe.backend.domain.sensor.dto.request;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SensorLogCreateRequest(
        Long workerId,
        Long equipmentId,
        @NotNull SensorType sensorType,
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
        Boolean sosPressed,
        RiskLevel riskLevel,
        LocalDateTime measuredAt
) {
}
