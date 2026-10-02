package com.worksafe.backend.domain.drone.control;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.worksafe.backend.domain.drone.control.adapter.HttpJetsonCommandAdapter;
import com.worksafe.backend.domain.drone.control.adapter.JetsonCommandProperties;
import com.worksafe.backend.domain.drone.control.port.JetsonCommand;
import com.worksafe.backend.domain.drone.control.port.JetsonDeliveryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class HttpJetsonCommandAdapterTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsJsonCommandWithBearerTokenAndMapsAcknowledgement() {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        server.createContext("/api/v1/drone/commands", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            respond(exchange, 200, """
                    {"status":"ACCEPTED","detail":"queued by jetson"}
                    """);
        });

        HttpJetsonCommandAdapter adapter = new HttpJetsonCommandAdapter(properties("secret-token"));
        JetsonDeliveryResult result = adapter.send(command());

        assertThat(result.status()).isEqualTo(JetsonDeliveryStatus.DELIVERED_TO_JETSON);
        assertThat(result.detail()).isEqualTo("queued by jetson");
        assertThat(authorization.get()).isEqualTo("Bearer secret-token");
        assertThat(requestBody.get())
                .contains("\"droneId\":1")
                .contains("\"droneSerialNumber\":\"S550-KOA-001\"")
                .contains("\"command\":\"ASCEND\"")
                .contains("\"durationSeconds\":2");
    }

    @Test
    void mapsJetsonHttpErrorWithoutReportingSuccessfulDelivery() {
        server.createContext("/api/v1/drone/commands", exchange -> respond(exchange, 503, "{}"));

        HttpJetsonCommandAdapter adapter = new HttpJetsonCommandAdapter(properties(null));
        JetsonDeliveryResult result = adapter.send(command());

        assertThat(result.status()).isEqualTo(JetsonDeliveryStatus.FAILED);
        assertThat(result.detail()).isEqualTo("Jetson HTTP response: 503");
    }

    private JetsonCommandProperties properties(String token) {
        return new JetsonCommandProperties(true, baseUrl, null, 1_000, 1_000, token);
    }

    private JetsonCommand command() {
        return new JetsonCommand(
                UUID.randomUUID(),
                1L,
                "S550-KOA-001",
                DroneControlCommand.ASCEND,
                2,
                OffsetDateTime.parse("2026-10-02T09:00:00Z")
        );
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
