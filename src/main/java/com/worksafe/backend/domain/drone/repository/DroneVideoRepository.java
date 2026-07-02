package com.worksafe.backend.domain.drone.repository;

import com.worksafe.backend.domain.drone.entity.DroneVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DroneVideoRepository extends JpaRepository<DroneVideo, Long> {

    Optional<DroneVideo> findFirstByDrone_IdAndActiveTrue(Long droneId);

    Optional<DroneVideo> findFirstByDispatch_IdOrderByCreatedAtDesc(Long dispatchId);
}
