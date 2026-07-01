package com.worksafe.backend.domain.equipment.entity;

import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
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
@Table(name = "wearable_commands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WearableCommand extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WearableCommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WearableCommandStatus commandStatus;

    @Column(nullable = false, length = 500)
    private String reason;

    private LocalDateTime requestedAt;
    private LocalDateTime acknowledgedAt;

    @Builder
    private WearableCommand(
            Worker worker,
            Equipment equipment,
            WearableCommandType commandType,
            WearableCommandStatus commandStatus,
            String reason,
            LocalDateTime requestedAt,
            LocalDateTime acknowledgedAt
    ) {
        this.worker = worker;
        this.equipment = equipment;
        this.commandType = commandType;
        this.commandStatus = commandStatus == null ? WearableCommandStatus.REQUESTED : commandStatus;
        this.reason = reason;
        this.requestedAt = requestedAt;
        this.acknowledgedAt = acknowledgedAt;
    }

    public void acknowledge() {
        this.commandStatus = WearableCommandStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
    }
}
