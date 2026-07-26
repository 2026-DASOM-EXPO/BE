package com.worksafe.backend.domain.drone.streaming;

public interface DroneStreamGateway {

    DroneStreamStatus start(String streamKey);

    DroneStreamStatus stop(String streamKey);

    DroneStreamStatus status(String streamKey);

    String playlistUrl(String streamKey);
}
