package com.worksafe.backend.domain.worker.entity;

import com.worksafe.backend.global.common.BaseEntity;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
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
@Table(name = "workers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Worker extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String rfidTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkerStatus status;

    private Double currentLatitude;
    private Double currentLongitude;

    @Builder
    private Worker(
            String name,
            String department,
            String phoneNumber,
            String rfidTag,
            WorkerStatus status,
            Double currentLatitude,
            Double currentLongitude
    ) {
        this.name = name;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.rfidTag = rfidTag;
        this.status = status == null ? WorkerStatus.NORMAL : status;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    public void update(
            String name,
            String department,
            String phoneNumber,
            String rfidTag,
            WorkerStatus status,
            Double currentLatitude,
            Double currentLongitude
    ) {
        if (name != null) {
            this.name = name;
        }
        if (department != null) {
            this.department = department;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (rfidTag != null) {
            this.rfidTag = rfidTag;
        }
        if (status != null) {
            this.status = status;
        }
        this.currentLatitude = currentLatitude != null ? currentLatitude : this.currentLatitude;
        this.currentLongitude = currentLongitude != null ? currentLongitude : this.currentLongitude;
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }
}
