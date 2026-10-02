package com.worksafe.backend.domain.drone.control.port;

import com.worksafe.backend.domain.drone.control.JetsonDeliveryStatus;

public record JetsonDeliveryResult(
        JetsonDeliveryStatus status,
        String detail
) {
    public static JetsonDeliveryResult notConfigured(String detail) {
        return new JetsonDeliveryResult(JetsonDeliveryStatus.NOT_CONFIGURED, detail);
    }
}
