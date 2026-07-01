package com.worksafe.backend.domain.drone.service;

import com.worksafe.backend.domain.drone.dto.request.DroneCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneVideoCreateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;

import java.util.List;

public interface DroneService {

    DroneResponse create(DroneCreateRequest request);

    List<DroneResponse> findAll();

    DroneResponse findById(Long droneId);

    DroneResponse update(Long droneId, DroneUpdateRequest request);

    void delete(Long droneId);

    DroneDispatchResponse dispatch(Long droneId, DroneDispatchCreateRequest request);

    List<DroneDispatchResponse> findAllDispatches();

    DroneDispatchResponse findDispatchById(Long dispatchId);

    DroneDispatchResponse updateDispatchStatus(Long dispatchId, DroneDispatchStatusUpdateRequest request);

    DroneVideoResponse createVideo(Long droneId, DroneVideoCreateRequest request);

    DroneVideoResponse startVideo(Long videoId);

    DroneVideoResponse stopVideo(Long videoId);

    DroneVideoResponse findActiveVideo(Long droneId);
}
