package com.worksafe.backend.domain.worker.service.impl;

import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.domain.worker.converter.WorkerConverter;
import com.worksafe.backend.domain.worker.dto.request.WorkerCreateRequest;
import com.worksafe.backend.domain.worker.dto.request.WorkerUpdateRequest;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import com.worksafe.backend.domain.worker.service.WorkerService;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;
    private final AlertRealtimeService alertRealtimeService;

    @Override
    public WorkerResponse create(WorkerCreateRequest request) {
        Worker worker = WorkerConverter.toEntity(request);
        Worker saved = workerRepository.save(worker);
        WorkerResponse response = WorkerConverter.toResponse(saved);
        alertRealtimeService.publish("worker", response);
        return response;
    }

    @Override
    public List<WorkerResponse> findAll() {
        return WorkerConverter.toResponseList(workerRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public WorkerResponse findById(Long workerId) {
        return WorkerConverter.toResponse(getWorker(workerId));
    }

    @Override
    public WorkerResponse update(Long workerId, WorkerUpdateRequest request) {
        Worker worker = getWorker(workerId);
        worker.update(
                request.name(),
                request.department(),
                request.phoneNumber(),
                request.status(),
                request.currentLatitude(),
                request.currentLongitude()
        );
        WorkerResponse response = WorkerConverter.toResponse(worker);
        alertRealtimeService.publish("worker", response);
        return response;
    }

    @Override
    public void delete(Long workerId) {
        Worker worker = getWorker(workerId);
        workerRepository.delete(worker);
        alertRealtimeService.publish("worker-deleted", Map.of("id", workerId));
    }

    @Override
    public WorkerResponse updateLocation(Long workerId, Double latitude, Double longitude) {
        Worker worker = getWorker(workerId);
        worker.updateLocation(latitude, longitude);
        WorkerResponse response = WorkerConverter.toResponse(worker);
        alertRealtimeService.publish("worker", response);
        return response;
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }
}
