package com.worksafe.backend.domain.equipment.repository;

import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findBySerialNumber(String serialNumber);

    List<Equipment> findByWorker_IdOrderByUpdatedAtDesc(Long workerId);

    Optional<Equipment> findFirstByWorker_IdOrderByUpdatedAtDesc(Long workerId);

    List<Equipment> findAllByOrderByCreatedAtDesc();

    long countByStatus(EquipmentStatus status);

    long countByWearStatus(WearStatus wearStatus);
}
