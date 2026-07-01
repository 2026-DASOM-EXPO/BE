package com.worksafe.backend.domain.alert.repository;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByReadStatusOrderByCreatedAtDesc(AlertReadStatus readStatus);

    long countByReadStatus(AlertReadStatus readStatus);
}
