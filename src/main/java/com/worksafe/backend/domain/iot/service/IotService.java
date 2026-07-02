package com.worksafe.backend.domain.iot.service;

import com.worksafe.backend.iot.dto.request.AttendanceRequest;
import com.worksafe.backend.iot.dto.request.BiometricRequest;
import com.worksafe.backend.iot.dto.request.DroneObstacleRequest;
import com.worksafe.backend.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.iot.dto.request.GpsRequest;
import com.worksafe.backend.iot.dto.request.ImuRequest;
import com.worksafe.backend.iot.dto.request.SosRequest;
import com.worksafe.backend.iot.dto.response.AttendanceResponse;
import com.worksafe.backend.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.sensor.dto.response.SensorLogResponse;

public interface IotService {

    AttendanceResponse attendance(AttendanceRequest request);

    SensorLogResponse biometrics(BiometricRequest request);

    SensorLogResponse imu(ImuRequest request);

    SensorLogResponse gps(GpsRequest request);

    SensorLogResponse equipmentStatus(EquipmentStatusRequest request);

    RiskEventResponse sos(SosRequest request);

    SensorLogResponse droneObstacle(DroneObstacleRequest request);
}
