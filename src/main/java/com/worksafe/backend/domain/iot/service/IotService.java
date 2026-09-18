package com.worksafe.backend.domain.iot.service;

import com.worksafe.backend.domain.iot.dto.request.BiometricRequest;
import com.worksafe.backend.domain.iot.dto.request.DroneObstacleRequest;
import com.worksafe.backend.domain.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.domain.iot.dto.request.GpsRequest;
import com.worksafe.backend.domain.iot.dto.request.ImuRequest;
import com.worksafe.backend.domain.iot.dto.request.HeartRequest;
import com.worksafe.backend.domain.iot.dto.request.SosRequest;
import com.worksafe.backend.domain.iot.dto.response.SosResponse;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;

public interface IotService {

    SensorLogResponse biometrics(BiometricRequest request);

    SensorLogResponse heart(HeartRequest request);

    SensorLogResponse imu(ImuRequest request);

    SensorLogResponse gps(GpsRequest request);

    SensorLogResponse equipmentStatus(EquipmentStatusRequest request);

    SosResponse sos(SosRequest request);

    SensorLogResponse droneObstacle(DroneObstacleRequest request);
}
