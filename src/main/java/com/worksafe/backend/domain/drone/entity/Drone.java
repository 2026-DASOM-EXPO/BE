package com.worksafe.backend.domain.drone.entity;

import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "drones")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Drone extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DroneStatus status;

    @Column(nullable = false)
    private Integer batteryPercent;

    private Double currentLatitude;
    private Double currentLongitude;
    private Integer maxFlightMinutes;

    @Column(nullable = false)
    private boolean payloadMounted;

    @Builder
    private Drone(
            String name,
            String serialNumber,
            String modelName,
            DroneStatus status,
            Integer batteryPercent,
            Double currentLatitude,
            Double currentLongitude,
            Integer maxFlightMinutes,
            boolean payloadMounted
    ) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.modelName = modelName;
        this.status = status == null ? DroneStatus.READY : status;
        this.batteryPercent = batteryPercent == null ? 100 : batteryPercent;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.maxFlightMinutes = maxFlightMinutes;
        this.payloadMounted = payloadMounted;
    }

    public void update(
            String name,
            String serialNumber,
            String modelName,
            DroneStatus status,
            Integer batteryPercent,
            Double currentLatitude,
            Double currentLongitude,
            Integer maxFlightMinutes,
            Boolean payloadMounted
    ) {
        if (name != null) {
            this.name = name;
        }
        if (serialNumber != null) {
            this.serialNumber = serialNumber;
        }
        if (modelName != null) {
            this.modelName = modelName;
        }
        if (status != null) {
            this.status = status;
        }
        if (batteryPercent != null) {
            this.batteryPercent = batteryPercent;
        }
        if (currentLatitude != null) {
            this.currentLatitude = currentLatitude;
        }
        if (currentLongitude != null) {
            this.currentLongitude = currentLongitude;
        }
        if (maxFlightMinutes != null) {
            this.maxFlightMinutes = maxFlightMinutes;
        }
        if (payloadMounted != null) {
            this.payloadMounted = payloadMounted;
        }
    }

    public void changeStatus(DroneStatus status) {
        this.status = status;
    }
}
