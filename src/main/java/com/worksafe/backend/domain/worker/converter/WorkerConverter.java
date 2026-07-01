package com.worksafe.backend.domain.worker.converter;

import com.worksafe.backend.domain.worker.dto.request.WorkerCreateRequest;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;
import com.worksafe.backend.domain.worker.entity.Worker;

import java.util.List;

public final class WorkerConverter {

    private WorkerConverter() {
    }

    public static Worker toEntity(WorkerCreateRequest request) {
        return Worker.builder()
                .name(request.name())
                .department(request.department())
                .phoneNumber(request.phoneNumber())
                .rfidTag(request.rfidTag())
                .status(request.status())
                .currentLatitude(request.currentLatitude())
                .currentLongitude(request.currentLongitude())
                .build();
    }

    public static WorkerResponse toResponse(Worker worker) {
        return new WorkerResponse(
                worker.getId(),
                worker.getName(),
                worker.getDepartment(),
                worker.getPhoneNumber(),
                worker.getRfidTag(),
                worker.getStatus(),
                worker.getCurrentLatitude(),
                worker.getCurrentLongitude(),
                worker.getCreatedAt(),
                worker.getUpdatedAt()
        );
    }

    public static List<WorkerResponse> toResponseList(List<Worker> workers) {
        return workers.stream().map(WorkerConverter::toResponse).toList();
    }
}
