package com.worksafe.backend.domain.drone.repository;

import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DroneDispatchRepository extends JpaRepository<DroneDispatch, Long> {

    List<DroneDispatch> findAllByOrderByCreatedAtDesc();

    List<DroneDispatch> findByDrone_IdOrderByCreatedAtDesc(Long droneId);

    List<DroneDispatch> findByRiskEvent_IdOrderByCreatedAtDesc(Long riskEventId);

    DroneDispatch findFirstByRiskEvent_IdOrderByCreatedAtDesc(Long riskEventId);

    long countByStatus(DroneDispatchStatus status);

    long countByStatusIn(Collection<DroneDispatchStatus> statuses);
}
