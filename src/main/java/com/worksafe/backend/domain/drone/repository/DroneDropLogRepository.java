package com.worksafe.backend.domain.drone.repository;

import com.worksafe.backend.domain.drone.entity.DroneDropLog;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneDropLogRepository extends JpaRepository<DroneDropLog, Long> {

    List<DroneDropLog> findByDroneDispatch_IdOrderByCreatedAtDesc(Long dispatchId);

    long countByDropStatus(DropStatus dropStatus);
}
