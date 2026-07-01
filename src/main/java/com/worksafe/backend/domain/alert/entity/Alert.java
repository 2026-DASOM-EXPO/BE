package com.worksafe.backend.domain.alert.entity;

import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.global.common.BaseEntity;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.worker.entity.Worker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "alerts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_event_id")
    private RiskEvent riskEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertReadStatus readStatus;

    private LocalDateTime readAt;

    @Builder
    private Alert(
            RiskEvent riskEvent,
            Worker worker,
            String title,
            String message,
            AlertSeverity severity,
            AlertReadStatus readStatus,
            LocalDateTime readAt
    ) {
        this.riskEvent = riskEvent;
        this.worker = worker;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.readStatus = readStatus == null ? AlertReadStatus.UNREAD : readStatus;
        this.readAt = readAt;
    }

    public void markAsRead() {
        this.readStatus = AlertReadStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}
