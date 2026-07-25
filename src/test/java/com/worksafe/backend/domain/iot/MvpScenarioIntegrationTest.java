package com.worksafe.backend.domain.iot;

import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.entity.DroneVideo;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.drone.repository.DroneVideoRepository;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.domain.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.domain.iot.dto.request.SosRequest;
import com.worksafe.backend.domain.iot.dto.response.SosResponse;
import com.worksafe.backend.domain.iot.service.IotService;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.risk.dto.request.RiskEventStatusUpdateRequest;
import com.worksafe.backend.domain.risk.service.RiskService;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MvpScenarioIntegrationTest {

    @Autowired
    private IotService iotService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RiskEventRepository riskEventRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private WearableCommandRepository wearableCommandRepository;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneDispatchRepository droneDispatchRepository;

    @Autowired
    private DroneVideoRepository droneVideoRepository;

    @Test
    void adcRangeIsZeroTo4095AndRepeatedSamplesAreAccepted() throws Exception {
        SafetyFixture fixture = saveSafetyFixture("adc");

        String valid = equipmentJson(fixture.worker(), fixture.helmet(), 4095);
        mockMvc.perform(post("/api/iot/equipment-status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valid))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.pressureValue").value(4095))
                .andExpect(jsonPath("$.data.wearStatus").value("WORN"))
                .andExpect(jsonPath("$.data.riskLevel").value("LV1"));

        mockMvc.perform(post("/api/iot/equipment-status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valid))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.wearStatus").value("WORN"));

        String invalid = equipmentJson(fixture.worker(), fixture.helmet(), 4096);
        mockMvc.perform(post("/api/iot/equipment-status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        String mismatchedEquipment = equipmentJson(fixture.worker(), fixture.vest(), 2000);
        mockMvc.perform(post("/api/iot/equipment-status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mismatchedEquipment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SENSOR_EQUIPMENT_TYPE"));
    }

    @Test
    void oneOrMoreMissingRequiredEquipmentAlwaysStaysLv2AndNeverDispatchesDrone() {
        SafetyFixture fixture = saveSafetyFixture("missing");
        saveReadyDrone("missing");

        SensorLogResponse first = iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.helmet(), 0, 1));
        SensorLogResponse second = iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.shoes(), 0, 2));
        fixture.vest().updateWearStatus(WearStatus.NOT_WORN, LocalDateTime.of(2026, 7, 24, 10, 2, 3));
        SensorLogResponse third = iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.helmet(), 0, 3));

        assertThat(first.riskLevel()).isEqualTo(RiskLevel.LV2);
        assertThat(second.riskLevel()).isEqualTo(RiskLevel.LV2);
        assertThat(third.riskLevel()).isEqualTo(RiskLevel.LV2);
        assertThat(riskEventRepository.findByWorker_IdOrderByOccurredAtDesc(fixture.worker().getId()))
                .filteredOn(event -> event.getRiskType() == RiskType.NO_EQUIPMENT)
                .extracting(RiskEvent::getRiskLevel)
                .containsOnly(RiskLevel.LV2);
        assertThat(droneDispatchRepository.count()).isZero();
    }

    @Test
    void lv2CreatesOneManagerAlertAndTargetsVestBuzzer() {
        SafetyFixture fixture = saveSafetyFixture("lv2");

        iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.shoes(), 20, 1));
        iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.shoes(), 30, 2));

        List<WearableCommand> commands = wearableCommandRepository.findByWorker_IdOrderByCreatedAtDesc(fixture.worker().getId());
        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(commands).hasSize(1);
        assertThat(commands.getFirst().getCommandType()).isEqualTo(WearableCommandType.BUZZER_ON);
        assertThat(commands.getFirst().getEquipment().getType()).isEqualTo(EquipmentType.VEST);
    }

    @Test
    void restoringAllRequiredEquipmentResolvesLv2AndQueuesBuzzerOff() {
        SafetyFixture fixture = saveSafetyFixture("restore");

        iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.shoes(), 20, 1));
        iotService.equipmentStatus(pressureRequest(fixture.worker(), fixture.shoes(), 4095, 2));

        assertThat(riskEventRepository.findByWorker_IdOrderByOccurredAtDesc(fixture.worker().getId()))
                .filteredOn(event -> event.getRiskType() == RiskType.NO_EQUIPMENT)
                .extracting(RiskEvent::getStatus)
                .containsOnly(RiskStatus.RESOLVED);
        assertThat(wearableCommandRepository.findByWorker_IdOrderByCreatedAtDesc(fixture.worker().getId()))
                .extracting(WearableCommand::getCommandType)
                .contains(WearableCommandType.BUZZER_ON, WearableCommandType.BUZZER_OFF);
        assertThat(workerRepository.findById(fixture.worker().getId()).orElseThrow().getStatus())
                .isEqualTo(WorkerStatus.NORMAL);
    }

    @Test
    void sosZeroDoesNotCreateEmergencyAndSosOneCreatesLv3AlertAndDispatchWithout119OrVideo() {
        SafetyFixture fixture = saveSafetyFixture("sos");
        saveReadyDrone("sos");

        SosResponse released = iotService.sos(sos(fixture, 0, 0));
        assertThat(released.triggered()).isFalse();
        assertThat(riskEventRepository.count()).isZero();
        assertThat(alertRepository.count()).isZero();
        assertThat(droneDispatchRepository.count()).isZero();

        SosResponse pressed = iotService.sos(sos(fixture, 1, 1));
        List<DroneDispatch> dispatches = droneDispatchRepository.findAllByOrderByCreatedAtDesc();

        assertThat(pressed.triggered()).isTrue();
        assertThat(pressed.riskEvent().riskLevel()).isEqualTo(RiskLevel.LV3);
        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(wearableCommandRepository.count()).isZero();
        assertThat(dispatches).hasSize(1);
        assertThat(dispatches.getFirst().isEmergencyCallRequested()).isFalse();
        assertThat(droneVideoRepository.count()).isZero();
    }

    @Test
    void managerConfirmationStartsOnlyOne720pVideoAndDuplicateSosDoesNotDuplicateDispatch() throws Exception {
        SafetyFixture fixture = saveSafetyFixture("confirm");
        Drone drone = saveReadyDrone("confirm");

        SosResponse first = iotService.sos(sos(fixture, 1, 1));
        SosResponse duplicate = iotService.sos(sos(fixture, 1, 2));

        assertThat(first.triggered()).isTrue();
        assertThat(duplicate.triggered()).isFalse();
        assertThat(duplicate.riskEvent().id()).isEqualTo(first.riskEvent().id());
        assertThat(alertRepository.count()).isEqualTo(1);
        assertThat(droneDispatchRepository.count()).isEqualTo(1);

        mockMvc.perform(patch("/api/risk-events/{riskEventId}/status", first.riskEvent().id())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PROCESSING"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        mockMvc.perform(get("/api/drones/{droneId}/videos/active", drone.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.protocol").value("HLS"))
                .andExpect(jsonPath("$.data.streamStatus").value("STREAMING"))
                .andExpect(jsonPath("$.data.width").value(1280))
                .andExpect(jsonPath("$.data.height").value(720))
                .andExpect(jsonPath("$.data.frameRate").value(30));

        List<DroneVideo> videos = droneVideoRepository.findAll();
        assertThat(videos).hasSize(1);
        assertThat(videos.getFirst().isActive()).isTrue();
        assertThat(videos.getFirst().getProtocol()).isEqualTo(VideoProtocol.HLS);
        assertThat(videos.getFirst().getWidth()).isEqualTo(1280);
        assertThat(videos.getFirst().getHeight()).isEqualTo(720);
    }

    private String equipmentJson(Worker worker, Equipment equipment, int pressureValue) {
        return """
                {
                  "workerId": %d,
                  "equipmentId": %d,
                  "pressureValue": %d,
                  "measuredAt": "2026-07-24T10:00:00"
                }
                """.formatted(worker.getId(), equipment.getId(), pressureValue);
    }

    private EquipmentStatusRequest pressureRequest(Worker worker, Equipment equipment, int pressure, int second) {
        return new EquipmentStatusRequest(
                worker.getId(),
                equipment.getId(),
                null,
                pressure,
                LocalDateTime.of(2026, 7, 24, 10, 2, second)
        );
    }

    private SosRequest sos(SafetyFixture fixture, int buttonValue, int second) {
        return new SosRequest(
                fixture.worker().getId(),
                fixture.vest().getId(),
                buttonValue,
                37.4979,
                127.0276,
                "SOS 통합 검증",
                LocalDateTime.of(2026, 7, 24, 10, 5, second)
        );
    }

    private SafetyFixture saveSafetyFixture(String suffix) {
        Worker worker = saveWorker(suffix);
        Equipment helmet = saveEquipment(worker, suffix + "-helmet", EquipmentType.HELMET, WearStatus.WORN);
        Equipment vest = saveEquipment(worker, suffix + "-vest", EquipmentType.VEST, WearStatus.WORN);
        Equipment shoes = saveEquipment(worker, suffix + "-shoes", EquipmentType.SHOES, WearStatus.WORN);
        return new SafetyFixture(worker, helmet, vest, shoes);
    }

    private Worker saveWorker(String suffix) {
        return workerRepository.save(Worker.builder()
                .name("검증 작업자 " + suffix)
                .department("MVP")
                .phoneNumber("010-0000-0000")
                .rfidTag("RFID-" + suffix)
                .status(WorkerStatus.NORMAL)
                .currentLatitude(37.4979)
                .currentLongitude(127.0276)
                .build());
    }

    private Equipment saveEquipment(
            Worker worker,
            String suffix,
            EquipmentType type,
            WearStatus wearStatus
    ) {
        return equipmentRepository.save(Equipment.builder()
                .worker(worker)
                .serialNumber("EQ-" + suffix)
                .name("검증 장비 " + suffix)
                .type(type)
                .status(EquipmentStatus.ASSIGNED)
                .wearStatus(wearStatus)
                .lastDetectedAt(LocalDateTime.of(2026, 7, 24, 9, 59))
                .build());
    }

    private Drone saveReadyDrone(String suffix) {
        return droneRepository.save(Drone.builder()
                .name("검증 드론 " + suffix)
                .serialNumber("DRONE-" + suffix)
                .modelName("SIYI A8 Mini")
                .status(DroneStatus.READY)
                .batteryPercent(100)
                .currentLatitude(37.4970)
                .currentLongitude(127.0270)
                .maxFlightMinutes(20)
                .payloadMounted(true)
                .build());
    }

    private record SafetyFixture(
            Worker worker,
            Equipment helmet,
            Equipment vest,
            Equipment shoes
    ) {
    }
}
