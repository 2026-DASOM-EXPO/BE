package com.worksafe.backend.domain.alert.repository;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByReadStatusOrderByCreatedAtDesc(AlertReadStatus readStatus);

    @EntityGraph(attributePaths = {"worker", "riskEvent", "riskEvent.worker"})
    List<Alert> findTop30ByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = {"worker", "riskEvent", "riskEvent.worker"})
    List<Alert> findTop30ByReadStatusOrderByCreatedAtDescIdDesc(AlertReadStatus readStatus);

    long countByReadStatus(AlertReadStatus readStatus);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Alert a
            SET a.readStatus = :readStatus,
                a.readAt = :readAt
            WHERE a.readStatus = :unreadStatus
            """)
    int markAllAsRead(
            @Param("unreadStatus") AlertReadStatus unreadStatus,
            @Param("readStatus") AlertReadStatus readStatus,
            @Param("readAt") LocalDateTime readAt
    );
}
