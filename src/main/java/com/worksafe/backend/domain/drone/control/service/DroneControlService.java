package com.worksafe.backend.domain.drone.control.service;

import com.worksafe.backend.domain.drone.control.dto.DroneControlRequest;
import com.worksafe.backend.domain.drone.control.dto.DroneControlResponse;

public interface DroneControlService {

    DroneControlResponse send(Long droneId, DroneControlRequest request);
}
