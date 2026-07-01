package com.worksafe.backend.domain.worker.service;

import com.worksafe.backend.domain.worker.dto.request.WorkerCreateRequest;
import com.worksafe.backend.domain.worker.dto.request.WorkerUpdateRequest;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;

import java.util.List;

public interface WorkerService {

    WorkerResponse create(WorkerCreateRequest request);

    List<WorkerResponse> findAll();

    WorkerResponse findById(Long workerId);

    WorkerResponse update(Long workerId, WorkerUpdateRequest request);

    void delete(Long workerId);

    WorkerResponse updateLocation(Long workerId, Double latitude, Double longitude);
}
