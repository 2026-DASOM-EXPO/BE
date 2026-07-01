package com.worksafe.backend.domain.drone.converter;

import com.worksafe.backend.domain.drone.dto.request.DroneCreateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.entity.DroneVideo;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;

import java.util.List;

public final class DroneConverter {

    private DroneConverter() {
    }

    public static Drone toEntity(DroneCreateRequest request) {
        return Drone.builder()
                .name(request.name())
                .serialNumber(request.serialNumber())
                .modelName(request.modelName())
                .status(request.status())
                .batteryPercent(request.batteryPercent())
                .currentLatitude(request.currentLatitude())
                .currentLongitude(request.currentLongitude())
                .maxFlightMinutes(request.maxFlightMinutes())
                .payloadMounted(Boolean.TRUE.equals(request.payloadMounted()))
                .build();
    }

    public static DroneResponse toResponse(Drone drone) {
        return new DroneResponse(
                drone.getId(),
                drone.getName(),
                drone.getSerialNumber(),
                drone.getModelName(),
                drone.getStatus(),
                drone.getBatteryPercent(),
                drone.getCurrentLatitude(),
                drone.getCurrentLongitude(),
                drone.getMaxFlightMinutes(),
                drone.isPayloadMounted(),
                drone.getCreatedAt(),
                drone.getUpdatedAt()
        );
    }

    public static DroneDispatchResponse toDispatchResponse(DroneDispatch dispatch) {
        return new DroneDispatchResponse(
                dispatch.getId(),
                toResponse(dispatch.getDrone()),
                dispatch.getRiskEvent() == null ? null : RiskEventConverter.toResponse(dispatch.getRiskEvent()),
                dispatch.getTargetLatitude(),
                dispatch.getTargetLongitude(),
                dispatch.getDispatchReason(),
                dispatch.isEmergencyKitMounted(),
                dispatch.isEmergencyKitDropped(),
                dispatch.getDropLatitude(),
                dispatch.getDropLongitude(),
                dispatch.getDropMethod(),
                dispatch.isEmergencyCallRequested(),
                dispatch.getEmergencyCallStatus(),
                dispatch.getStatus(),
                dispatch.getCommandMessage(),
                dispatch.getDispatchedAt(),
                dispatch.getArrivedAt(),
                dispatch.getCompletedAt(),
                dispatch.getCreatedAt(),
                dispatch.getUpdatedAt()
        );
    }

    public static DroneVideoResponse toVideoResponse(DroneVideo video) {
        return new DroneVideoResponse(
                video.getId(),
                toResponse(video.getDrone()),
                video.getDispatch() == null ? null : video.getDispatch().getId(),
                video.getTitle(),
                video.getDescription(),
                video.getStreamUrl(),
                video.getProtocol(),
                video.isActive(),
                video.getStreamStatus(),
                video.getStartedAt(),
                video.getEndedAt(),
                video.getLastFrameAt(),
                video.getCreatedAt()
        );
    }

    public static List<DroneResponse> toResponseList(List<Drone> drones) {
        return drones.stream().map(DroneConverter::toResponse).toList();
    }

    public static List<DroneDispatchResponse> toDispatchResponseList(List<DroneDispatch> dispatches) {
        return dispatches.stream().map(DroneConverter::toDispatchResponse).toList();
    }
}
