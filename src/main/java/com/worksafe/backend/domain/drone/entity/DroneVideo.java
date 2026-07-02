package com.worksafe.backend.domain.drone.entity;

import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import com.worksafe.backend.domain.drone.enums.StreamStatus;
import com.worksafe.backend.global.common.BaseEntity;
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
@Table(name = "drone_videos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DroneVideo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id")
    private DroneDispatch dispatch;

    @Column(length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 500)
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoProtocol protocol;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StreamStatus streamStatus;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime lastFrameAt;

    @Builder
    private DroneVideo(
            Drone drone,
            DroneDispatch dispatch,
            String title,
            String description,
            String streamUrl,
            VideoProtocol protocol,
            boolean active,
            StreamStatus streamStatus,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            LocalDateTime lastFrameAt
    ) {
        this.drone = drone;
        this.dispatch = dispatch;
        this.title = title;
        this.description = description;
        this.streamUrl = streamUrl;
        this.protocol = protocol;
        this.active = active;
        this.streamStatus = streamStatus == null ? StreamStatus.READY : streamStatus;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.lastFrameAt = lastFrameAt;
    }

    public void deactivate() {
        this.active = false;
        this.endedAt = LocalDateTime.now();
        this.streamStatus = StreamStatus.STOPPED;
    }

    public void start() {
        this.active = true;
        this.startedAt = LocalDateTime.now();
        this.streamStatus = StreamStatus.STREAMING;
    }

    public void stop() {
        this.active = false;
        this.endedAt = LocalDateTime.now();
        this.streamStatus = StreamStatus.STOPPED;
    }

    public void markFrameReceived() {
        this.lastFrameAt = LocalDateTime.now();
    }
}
