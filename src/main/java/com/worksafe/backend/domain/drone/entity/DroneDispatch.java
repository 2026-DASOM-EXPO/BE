package com.worksafe.backend.domain.drone.entity;

import com.worksafe.backend.global.common.BaseEntity;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.EmergencyCallStatus;
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
@Table(name = "drone_dispatches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DroneDispatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_event_id")
    private RiskEvent riskEvent;

    private Double targetLatitude;
    private Double targetLongitude;
    private String dispatchReason;
    private boolean emergencyKitMounted;
    private boolean emergencyKitDropped;
    private Double dropLatitude;
    private Double dropLongitude;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DropMethod dropMethod;
    @Column(nullable = false)
    private boolean emergencyCallRequested;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmergencyCallStatus emergencyCallStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DroneDispatchStatus status;

    @Column(nullable = false, length = 500)
    private String commandMessage;

    private LocalDateTime dispatchedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime completedAt;

    @Builder
    private DroneDispatch(
            Drone drone,
            RiskEvent riskEvent,
            Double targetLatitude,
            Double targetLongitude,
            String dispatchReason,
            Boolean emergencyKitMounted,
            Boolean emergencyKitDropped,
            Double dropLatitude,
            Double dropLongitude,
            DropMethod dropMethod,
            Boolean emergencyCallRequested,
            EmergencyCallStatus emergencyCallStatus,
            DroneDispatchStatus status,
            String commandMessage,
            LocalDateTime dispatchedAt,
            LocalDateTime arrivedAt,
            LocalDateTime completedAt
    ) {
        this.drone = drone;
        this.riskEvent = riskEvent;
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.dispatchReason = dispatchReason;
        this.emergencyKitMounted = emergencyKitMounted == null || emergencyKitMounted;
        this.emergencyKitDropped = emergencyKitDropped != null && emergencyKitDropped;
        this.dropLatitude = dropLatitude;
        this.dropLongitude = dropLongitude;
        this.dropMethod = dropMethod;
        this.emergencyCallRequested = emergencyCallRequested != null && emergencyCallRequested;
        this.emergencyCallStatus = emergencyCallStatus == null ? EmergencyCallStatus.NOT_REQUESTED : emergencyCallStatus;
        this.status = status == null ? DroneDispatchStatus.REQUESTED : status;
        this.commandMessage = commandMessage;
        this.dispatchedAt = dispatchedAt;
        this.arrivedAt = arrivedAt;
        this.completedAt = completedAt;
    }

    public void changeStatus(DroneDispatchStatus status) {
        this.status = status;
        if (status == DroneDispatchStatus.ARRIVED) {
            this.arrivedAt = LocalDateTime.now();
        }
        if (status == DroneDispatchStatus.KIT_DROPPED) {
            this.emergencyKitDropped = true;
        }
        if (status == DroneDispatchStatus.RETURNED
                || status == DroneDispatchStatus.FAILED
                || status == DroneDispatchStatus.CANCELED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void markEmergencyCallRequested() {
        this.emergencyCallRequested = true;
        this.emergencyCallStatus = EmergencyCallStatus.REQUESTED;
    }

    public void markEmergencyCallCompleted() {
        this.emergencyCallStatus = EmergencyCallStatus.COMPLETED;
    }

    public void markEmergencyCallFailed() {
        this.emergencyCallStatus = EmergencyCallStatus.FAILED;
    }

    public void markKitDropped(Double dropLatitude, Double dropLongitude, DropMethod dropMethod) {
        this.emergencyKitDropped = true;
        this.dropLatitude = dropLatitude;
        this.dropLongitude = dropLongitude;
        this.dropMethod = dropMethod;
        this.status = DroneDispatchStatus.KIT_DROPPED;
    }
}
