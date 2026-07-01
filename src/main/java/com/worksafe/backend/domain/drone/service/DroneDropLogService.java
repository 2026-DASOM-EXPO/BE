package com.worksafe.backend.domain.drone.service;

import com.worksafe.backend.domain.drone.dto.request.DroneDropLogCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDropLogStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDropLogResponse;

import java.util.List;

public interface DroneDropLogService {

    DroneDropLogResponse create(Long dispatchId, DroneDropLogCreateRequest request);

    List<DroneDropLogResponse> findByDispatchId(Long dispatchId);

    DroneDropLogResponse updateStatus(Long dropLogId, DroneDropLogStatusUpdateRequest request);
}
