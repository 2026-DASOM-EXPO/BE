package com.worksafe.backend.domain.drone.repository;

import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drone, Long> {

    List<Drone> findAllByOrderByCreatedAtDesc();

    Optional<Drone> findFirstByStatus(DroneStatus status);

    long countByStatus(DroneStatus status);
}
