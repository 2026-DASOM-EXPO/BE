package com.worksafe.backend.domain.equipment.repository;

import com.worksafe.backend.domain.equipment.entity.EquipmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentLogRepository extends JpaRepository<EquipmentLog, Long> {

    List<EquipmentLog> findAllByOrderByIssuedAtDesc();

    List<EquipmentLog> findByWorker_IdOrderByIssuedAtDesc(Long workerId);

    List<EquipmentLog> findByEquipment_IdOrderByIssuedAtDesc(Long equipmentId);

    Optional<EquipmentLog> findTopByEquipment_IdAndReturnedAtIsNullOrderByIssuedAtDesc(Long equipmentId);

    Optional<EquipmentLog> findTopByEquipment_IdOrderByIssuedAtDesc(Long equipmentId);
}
