package com.worksafe.backend.domain.sensor.repository;

import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.sensor.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    List<SensorLog> findByWorker_IdOrderByMeasuredAtDesc(Long workerId);

    List<SensorLog> findByEquipment_IdOrderByMeasuredAtDesc(Long equipmentId);

    List<SensorLog> findAllByOrderByMeasuredAtDesc();

    SensorLog findTopByWorker_IdOrderByMeasuredAtDesc(Long workerId);

    SensorLog findTopByWorker_IdAndSensorTypeOrderByMeasuredAtDesc(Long workerId, SensorType sensorType);
}
