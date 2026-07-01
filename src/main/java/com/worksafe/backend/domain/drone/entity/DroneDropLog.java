package com.worksafe.backend.domain.drone.entity;

import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.DropStatus;
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

@Getter
@Entity
@Table(name = "drone_drop_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DroneDropLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_dispatch_id", nullable = false)
    private DroneDispatch droneDispatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_event_id")
    private RiskEvent riskEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DropMethod dropMethod;

    private Double targetLatitude;
    private Double targetLongitude;
    private Double actualDropLatitude;
    private Double actualDropLongitude;
    private boolean obstacleDetected;
    private Double lidarFrontLeft;
    private Double lidarFrontRight;
    private Double lidarBackLeft;
    private Double lidarBackRight;
    private Double lidarSideLeft;
    private Double lidarSideRight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DropStatus dropStatus;

    @Builder
    private DroneDropLog(
            DroneDispatch droneDispatch,
            Drone drone,
            Worker worker,
            RiskEvent riskEvent,
            DropMethod dropMethod,
            Double targetLatitude,
            Double targetLongitude,
            Double actualDropLatitude,
            Double actualDropLongitude,
            boolean obstacleDetected,
            Double lidarFrontLeft,
            Double lidarFrontRight,
            Double lidarBackLeft,
            Double lidarBackRight,
            Double lidarSideLeft,
            Double lidarSideRight,
            DropStatus dropStatus
    ) {
        this.droneDispatch = droneDispatch;
        this.drone = drone;
        this.worker = worker;
        this.riskEvent = riskEvent;
        this.dropMethod = dropMethod;
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.actualDropLatitude = actualDropLatitude;
        this.actualDropLongitude = actualDropLongitude;
        this.obstacleDetected = obstacleDetected;
        this.lidarFrontLeft = lidarFrontLeft;
        this.lidarFrontRight = lidarFrontRight;
        this.lidarBackLeft = lidarBackLeft;
        this.lidarBackRight = lidarBackRight;
        this.lidarSideLeft = lidarSideLeft;
        this.lidarSideRight = lidarSideRight;
        this.dropStatus = dropStatus == null ? DropStatus.READY : dropStatus;
    }

    public void changeStatus(DropStatus dropStatus) {
        this.dropStatus = dropStatus;
    }

    public void updateActualPosition(Double actualDropLatitude, Double actualDropLongitude) {
        if (actualDropLatitude != null) {
            this.actualDropLatitude = actualDropLatitude;
        }
        if (actualDropLongitude != null) {
            this.actualDropLongitude = actualDropLongitude;
        }
    }

    public void markObstacleDetected(boolean obstacleDetected) {
        this.obstacleDetected = obstacleDetected;
    }
}
