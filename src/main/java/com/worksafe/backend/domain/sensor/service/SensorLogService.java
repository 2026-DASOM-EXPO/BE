package com.worksafe.backend.domain.sensor.service;

import com.worksafe.backend.domain.sensor.dto.request.SensorLogCreateRequest;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;

import java.util.List;

public interface SensorLogService {

    SensorLogResponse create(SensorLogCreateRequest request);

    List<SensorLogResponse> findAll();

    List<SensorLogResponse> findByWorkerId(Long workerId);

    List<SensorLogResponse> findByEquipmentId(Long equipmentId);

    SensorLogResponse findLatestByWorkerId(Long workerId);
}
