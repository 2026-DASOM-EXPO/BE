package com.worksafe.backend.domain.equipment.entity;

import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.global.common.BaseEntity;
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
@Table(name = "equipment_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WearStatus wearStatus;

    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;

    @Builder
    private EquipmentLog(Worker worker, Equipment equipment, WearStatus wearStatus, LocalDateTime issuedAt, LocalDateTime returnedAt) {
        this.worker = worker;
        this.equipment = equipment;
        this.wearStatus = wearStatus;
        this.issuedAt = issuedAt;
        this.returnedAt = returnedAt;
    }

    public void markReturned(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}
