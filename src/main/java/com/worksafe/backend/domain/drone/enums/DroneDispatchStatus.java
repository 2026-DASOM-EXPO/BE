package com.worksafe.backend.domain.drone.enums;

public enum DroneDispatchStatus {
    REQUESTED,
    DISPATCHED,
    ARRIVED,
    KIT_DROPPING,
    KIT_DROPPED,
    RETURNED,
    FAILED,
    CANCELED
}
