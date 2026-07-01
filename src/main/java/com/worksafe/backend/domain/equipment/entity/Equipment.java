package com.worksafe.backend.domain.equipment.entity;

import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
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
@Table(name = "equipment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @Column(nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WearStatus wearStatus;

    private LocalDateTime lastDetectedAt;

    @Column(nullable = false)
    private boolean buzzerEnabled;

    @Column(nullable = false)
    private boolean workTimerEnabled;

    private LocalDateTime workTimerStartedAt;
    private LocalDateTime workTimerEndedAt;

    @Column(nullable = false)
    private boolean manualWearOverride;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WearStatus manualWearStatus;

    @Builder
    private Equipment(
            Worker worker,
            String serialNumber,
            String name,
            EquipmentType type,
            EquipmentStatus status,
            WearStatus wearStatus,
            LocalDateTime lastDetectedAt
    ) {
        this.worker = worker;
        this.serialNumber = serialNumber;
        this.name = name;
        this.type = type;
        this.status = status == null ? EquipmentStatus.AVAILABLE : status;
        this.wearStatus = wearStatus == null ? WearStatus.UNKNOWN : wearStatus;
        this.lastDetectedAt = lastDetectedAt;
        this.buzzerEnabled = false;
        this.workTimerEnabled = false;
        this.manualWearOverride = false;
        this.manualWearStatus = null;
    }

    public void assignTo(Worker worker) {
        this.worker = worker;
        this.status = EquipmentStatus.ASSIGNED;
    }

    public void update(
            String serialNumber,
            String name,
            EquipmentType type,
            EquipmentStatus status,
            WearStatus wearStatus,
            LocalDateTime lastDetectedAt
    ) {
        if (serialNumber != null) {
            this.serialNumber = serialNumber;
        }
        if (name != null) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        if (status != null) {
            this.status = status;
        }
        if (wearStatus != null) {
            this.wearStatus = wearStatus;
        }
        if (lastDetectedAt != null) {
            this.lastDetectedAt = lastDetectedAt;
        }
    }

    public void updateWearStatus(WearStatus wearStatus, LocalDateTime detectedAt) {
        if (manualWearOverride) {
            this.lastDetectedAt = detectedAt;
            return;
        }
        this.wearStatus = wearStatus;
        this.lastDetectedAt = detectedAt;
    }

    public void setManualWearStatus(WearStatus wearStatus) {
        this.manualWearOverride = true;
        this.manualWearStatus = wearStatus;
        this.wearStatus = wearStatus;
    }

    public void clearManualWearOverride() {
        this.manualWearOverride = false;
        this.manualWearStatus = null;
    }

    public void setBuzzerEnabled(boolean enabled) {
        this.buzzerEnabled = enabled;
    }

    public void startWorkTimer(LocalDateTime startedAt) {
        this.workTimerEnabled = true;
        this.workTimerStartedAt = startedAt;
        this.workTimerEndedAt = null;
    }

    public void stopWorkTimer(LocalDateTime endedAt) {
        this.workTimerEnabled = false;
        this.workTimerEndedAt = endedAt;
    }
}
