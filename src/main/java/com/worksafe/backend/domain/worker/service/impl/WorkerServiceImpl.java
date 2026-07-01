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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;

    @Override
    public WorkerResponse create(WorkerCreateRequest request) {
        Worker worker = WorkerConverter.toEntity(request);
        return WorkerConverter.toResponse(workerRepository.save(worker));
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
                request.rfidTag(),
                request.status(),
                request.currentLatitude(),
                request.currentLongitude()
        );
        return WorkerConverter.toResponse(worker);
    }

    @Override
    public void delete(Long workerId) {
        Worker worker = getWorker(workerId);
        workerRepository.delete(worker);
    }

    @Override
    public WorkerResponse updateLocation(Long workerId, Double latitude, Double longitude) {
        Worker worker = getWorker(workerId);
        worker.updateLocation(latitude, longitude);
        return WorkerConverter.toResponse(worker);
    }

    private Worker getWorker(Long workerId) {
        return workerRepository.findById(workerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKER_NOT_FOUND));
    }
}
