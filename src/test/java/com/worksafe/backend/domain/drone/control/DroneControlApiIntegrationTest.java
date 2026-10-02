package com.worksafe.backend.domain.drone.control;

import com.worksafe.backend.domain.drone.control.port.JetsonCommand;
import com.worksafe.backend.domain.drone.control.port.JetsonCommandPort;
import com.worksafe.backend.domain.drone.control.port.JetsonDeliveryResult;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DroneControlApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DroneRepository droneRepository;

    @MockitoBean
    private JetsonCommandPort jetsonCommandPort;

    private Drone drone;

    @BeforeEach
    void setUp() {
        droneRepository.deleteAll();
        reset(jetsonCommandPort);
        when(jetsonCommandPort.send(any())).thenReturn(JetsonDeliveryResult.notConfigured("test transport"));
        drone = droneRepository.save(Drone.builder()
                .name("S550-01")
                .serialNumber("S550-KOA-001")
                .modelName("S550 Hexacopter")
                .status(DroneStatus.READY)
                .batteryPercent(100)
                .payloadMounted(false)
                .build());
    }

    @Test
    void omittedDurationDefaultsToOneSecondAndIsPassedToJetsonPort() throws Exception {
        mockMvc.perform(post("/api/drones/{droneId}/commands", drone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"command":"FORWARD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("명령 접수"))
                .andExpect(jsonPath("$.data.command").value("FORWARD"))
                .andExpect(jsonPath("$.data.durationSeconds").value(1))
                .andExpect(jsonPath("$.data.jetsonDeliveryStatus").value("NOT_CONFIGURED"));

        ArgumentCaptor<JetsonCommand> captor = ArgumentCaptor.forClass(JetsonCommand.class);
        verify(jetsonCommandPort).send(captor.capture());
        JetsonCommand sent = captor.getValue();
        assertThat(sent.droneId()).isEqualTo(drone.getId());
        assertThat(sent.droneSerialNumber()).isEqualTo("S550-KOA-001");
        assertThat(sent.command()).isEqualTo(DroneControlCommand.FORWARD);
        assertThat(sent.durationSeconds()).isEqualTo(1);
        assertThat(sent.commandId()).isNotNull();
        assertThat(sent.issuedAt()).isNotNull();
    }

    @Test
    void explicitDurationIsPassedUnchanged() throws Exception {
        mockMvc.perform(post("/api/drones/{droneId}/commands", drone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"command":"YAW_LEFT","durationSeconds":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.durationSeconds").value(3));

        ArgumentCaptor<JetsonCommand> captor = ArgumentCaptor.forClass(JetsonCommand.class);
        verify(jetsonCommandPort).send(captor.capture());
        assertThat(captor.getValue().command()).isEqualTo(DroneControlCommand.YAW_LEFT);
        assertThat(captor.getValue().durationSeconds()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void nonPositiveDurationIsRejectedByValidation(int durationSeconds) throws Exception {
        mockMvc.perform(post("/api/drones/{droneId}/commands", drone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"command":"ASCEND","durationSeconds":%d}
                                """.formatted(durationSeconds)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(jetsonCommandPort, never()).send(any());
    }

    @Test
    void unregisteredDroneIsRejectedBeforeJetsonPortCall() throws Exception {
        mockMvc.perform(post("/api/drones/{droneId}/commands", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"command":"HOVER"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRONE_NOT_FOUND"));

        verify(jetsonCommandPort, never()).send(any());
    }
}
