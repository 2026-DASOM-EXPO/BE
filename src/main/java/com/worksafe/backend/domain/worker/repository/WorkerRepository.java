package com.worksafe.backend.domain.worker.repository;

import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findAllByOrderByCreatedAtDesc();

    long countByStatus(WorkerStatus status);
}
