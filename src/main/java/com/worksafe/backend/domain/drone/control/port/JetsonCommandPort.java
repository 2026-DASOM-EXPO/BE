package com.worksafe.backend.domain.drone.control.port;

public interface JetsonCommandPort {

    JetsonDeliveryResult send(JetsonCommand command);
}
