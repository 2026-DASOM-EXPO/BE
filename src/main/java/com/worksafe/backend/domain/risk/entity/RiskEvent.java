package com.worksafe.backend.domain.risk.entity;

import com.worksafe.backend.global.common.BaseEntity;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
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
@Table(name = "risk_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RiskType riskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false, length = 500)
    private String description;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskStatus status;

    private LocalDateTime occurredAt;
    private LocalDateTime resolvedAt;

    @Builder
    private RiskEvent(
            Worker worker,
            RiskSourceType sourceType,
            RiskType riskType,
            RiskLevel riskLevel,
            String description,
            Double latitude,
            Double longitude,
            RiskStatus status,
            LocalDateTime occurredAt,
            LocalDateTime resolvedAt
    ) {
        this.worker = worker;
        this.sourceType = sourceType;
        this.riskType = riskType;
        this.riskLevel = riskLevel;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status == null ? RiskStatus.OPEN : status;
        this.occurredAt = occurredAt;
        this.resolvedAt = resolvedAt;
    }

    public void changeStatus(RiskStatus status) {
        this.status = status;
        if (status == RiskStatus.RESOLVED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}
