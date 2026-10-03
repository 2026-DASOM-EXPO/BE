package com.worksafe.backend.domain.drone.control.adapter;

import com.worksafe.backend.domain.drone.control.port.JetsonCommand;
import com.worksafe.backend.domain.drone.control.port.JetsonCommandPort;
import com.worksafe.backend.domain.drone.control.port.JetsonDeliveryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.drone.jetson",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NotConfiguredJetsonCommandAdapter implements JetsonCommandPort {

    @Override
    public JetsonDeliveryResult send(JetsonCommand command) {
        // TODO: Jetson의 HTTP/MQTT 계약이 확정되면 실제 네트워크 어댑터로 교체한다.
        return JetsonDeliveryResult.notConfigured(
                "Jetson command transport is not configured; no hardware command was sent."
        );
    }
}
