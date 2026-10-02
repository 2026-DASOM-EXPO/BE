package com.worksafe.backend.domain.drone.control.adapter;

import com.worksafe.backend.domain.drone.control.JetsonDeliveryStatus;
import com.worksafe.backend.domain.drone.control.port.JetsonCommand;
import com.worksafe.backend.domain.drone.control.port.JetsonCommandPort;
import com.worksafe.backend.domain.drone.control.port.JetsonDeliveryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "app.drone.jetson", name = "enabled", havingValue = "true")
public class HttpJetsonCommandAdapter implements JetsonCommandPort {

    private static final Set<String> ACCEPTED_STATUSES = Set.of("ACCEPTED", "QUEUED", "DELIVERED");

    private final JetsonCommandProperties properties;
    private final RestClient restClient;

    public HttpJetsonCommandAdapter(JetsonCommandProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.resolvedConnectTimeoutMillis()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.resolvedReadTimeoutMillis()));

        this.restClient = RestClient.builder()
                .baseUrl(properties.requiredBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public JetsonDeliveryResult send(JetsonCommand command) {
        try {
            JetsonAcknowledgement acknowledgement = restClient.post()
                    .uri(properties.resolvedCommandPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(this::applyAuthorization)
                    .body(command)
                    .retrieve()
                    .body(JetsonAcknowledgement.class);

            if (acknowledgement == null || acknowledgement.status() == null) {
                return failed("Jetson returned no command acknowledgement");
            }

            String status = acknowledgement.status().trim().toUpperCase(Locale.ROOT);
            if (!ACCEPTED_STATUSES.contains(status)) {
                return failed(detailOrDefault(acknowledgement.detail(), "Jetson rejected the command: " + status));
            }
            return new JetsonDeliveryResult(
                    JetsonDeliveryStatus.DELIVERED_TO_JETSON,
                    detailOrDefault(acknowledgement.detail(), "Jetson accepted the command")
            );
        } catch (RestClientResponseException exception) {
            return failed("Jetson HTTP response: " + exception.getStatusCode().value());
        } catch (RestClientException exception) {
            return failed("Jetson connection failed: " + exception.getClass().getSimpleName());
        }
    }

    private void applyAuthorization(HttpHeaders headers) {
        if (properties.hasAuthToken()) {
            headers.setBearerAuth(properties.resolvedAuthToken());
        }
    }

    private JetsonDeliveryResult failed(String detail) {
        return new JetsonDeliveryResult(JetsonDeliveryStatus.FAILED, detail);
    }

    private String detailOrDefault(String detail, String defaultValue) {
        return detail == null || detail.isBlank() ? defaultValue : detail;
    }

    private record JetsonAcknowledgement(String status, String detail) {
    }
}
