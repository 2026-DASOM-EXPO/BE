package com.worksafe.backend.domain.dashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.entity.DroneDispatch;
import com.worksafe.backend.domain.drone.entity.DroneDropLog;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.DropMethod;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneDropLogRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.equipment.entity.Equipment;
import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.EquipmentStatus;
import com.worksafe.backend.domain.equipment.enums.EquipmentType;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RiskEventRepository riskEventRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneDispatchRepository droneDispatchRepository;

    @Autowired
    private DroneDropLogRepository droneDropLogRepository;

    @Autowired
    private WearableCommandRepository wearableCommandRepository;

    @Test
    void workerStatusReturnsTotalAndCountsByStatus() throws Exception {
        saveWorker("normal-1", WorkerStatus.NORMAL);
        saveWorker("normal-2", WorkerStatus.NORMAL);
        saveWorker("warning", WorkerStatus.WARNING);
        saveWorker("danger", WorkerStatus.DANGER);
        saveWorker("inactive", WorkerStatus.INACTIVE);

        mockMvc.perform(get("/api/dashboard/workers/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.totalWorkers").value(5))
                .andExpect(jsonPath("$.data.normalWorkers").value(2))
                .andExpect(jsonPath("$.data.warningWorkers").value(1))
                .andExpect(jsonPath("$.data.dangerWorkers").value(1))
                .andExpect(jsonPath("$.data.inactiveWorkers").value(1));
    }

    @Test
    void equipmentStatusReturnsTotalAndCountsByWearStatus() throws Exception {
        saveEquipment("worn-1", WearStatus.WORN, null);
        saveEquipment("worn-2", WearStatus.WORN, null);
        saveEquipment("not-worn", WearStatus.NOT_WORN, null);
        saveEquipment("unknown", WearStatus.UNKNOWN, null);

        mockMvc.perform(get("/api/dashboard/equipment/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.totalEquipment").value(4))
                .andExpect(jsonPath("$.data.wornEquipment").value(2))
                .andExpect(jsonPath("$.data.notWornEquipment").value(1))
                .andExpect(jsonPath("$.data.unknownEquipment").value(1));
    }

    @Test
    void droneStatusReturnsCountsForEveryStatus() throws Exception {
        saveDrone("ready-1", DroneStatus.READY);
        saveDrone("ready-2", DroneStatus.READY);
        saveDrone("flying", DroneStatus.FLYING);
        saveDrone("returning", DroneStatus.RETURNING);
        saveDrone("charging", DroneStatus.CHARGING);
        saveDrone("maintenance", DroneStatus.MAINTENANCE);
        saveDrone("disabled", DroneStatus.DISABLED);

        mockMvc.perform(get("/api/dashboard/drones/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.readyDrones").value(2))
                .andExpect(jsonPath("$.data.flyingDrones").value(1))
                .andExpect(jsonPath("$.data.returningDrones").value(1))
                .andExpect(jsonPath("$.data.chargingDrones").value(1))
                .andExpect(jsonPath("$.data.maintenanceDrones").value(1))
                .andExpect(jsonPath("$.data.disabledDrones").value(1));
    }

    @Test
    void recentRiskEventsReturnsOnlyLatestTenActiveEventsInStableOrder() throws Exception {
        Worker worker = saveWorker("risk-worker", WorkerStatus.WARNING);
        LocalDateTime latestOccurredAt = LocalDateTime.of(2026, 7, 15, 12, 0);
        List<RiskEvent> activeEvents = new ArrayList<>();

        activeEvents.add(saveRiskEvent(worker, RiskLevel.LV2, RiskStatus.OPEN, latestOccurredAt));
        activeEvents.add(saveRiskEvent(worker, RiskLevel.LV3, RiskStatus.PROCESSING, latestOccurredAt));
        for (int minute = 1; minute <= 10; minute++) {
            RiskStatus status = minute % 2 == 0 ? RiskStatus.OPEN : RiskStatus.PROCESSING;
            activeEvents.add(saveRiskEvent(worker, RiskLevel.LV2, status, latestOccurredAt.minusMinutes(minute)));
        }
        saveRiskEvent(worker, RiskLevel.LV4, RiskStatus.RESOLVED, latestOccurredAt.plusMinutes(1));
        riskEventRepository.flush();

        List<Long> expectedIds = activeEvents.stream()
                .sorted(Comparator.comparing(RiskEvent::getOccurredAt).reversed()
                        .thenComparing(RiskEvent::getId, Comparator.reverseOrder()))
                .limit(10)
                .map(RiskEvent::getId)
                .toList();

        MvcResult result = mockMvc.perform(get("/api/dashboard/risk-events/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(10))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("data");
        List<Long> actualIds = new ArrayList<>();
        data.forEach(event -> {
            actualIds.add(event.get("id").asLong());
            assertThat(event.get("status").asText()).isIn("OPEN", "PROCESSING");
            assertThat(event.get("worker").get("id").asLong()).isEqualTo(worker.getId());
            assertThat(event.get("worker").get("name").asText()).isEqualTo("risk-worker");
        });

        assertThat(actualIds).containsExactlyElementsOf(expectedIds);
    }

    @Test
    void summaryReturnsAllDashboardAggregates() throws Exception {
        Worker normalWorker = saveWorker("summary-normal", WorkerStatus.NORMAL);
        saveWorker("summary-warning", WorkerStatus.WARNING);
        saveWorker("summary-danger", WorkerStatus.DANGER);
        saveWorker("summary-inactive", WorkerStatus.INACTIVE);

        Equipment wornEquipment = saveEquipment("summary-worn", WearStatus.WORN, normalWorker);
        saveEquipment("summary-not-worn", WearStatus.NOT_WORN, normalWorker);
        saveEquipment("summary-unknown", WearStatus.UNKNOWN, null);

        LocalDateTime occurredAt = LocalDateTime.of(2026, 7, 15, 10, 0);
        RiskEvent lv1 = saveRiskEvent(normalWorker, RiskLevel.LV1, RiskStatus.OPEN, occurredAt);
        saveRiskEvent(normalWorker, RiskLevel.LV2, RiskStatus.PROCESSING, occurredAt.plusMinutes(1));
        saveRiskEvent(normalWorker, RiskLevel.LV3, RiskStatus.RESOLVED, occurredAt.plusMinutes(2));
        saveRiskEvent(normalWorker, RiskLevel.LV4, RiskStatus.OPEN, occurredAt.plusMinutes(3));

        saveAlert("unread-1", AlertReadStatus.UNREAD, normalWorker, lv1);
        saveAlert("unread-2", AlertReadStatus.UNREAD, normalWorker, lv1);
        saveAlert("read", AlertReadStatus.READ, normalWorker, lv1);

        Drone readyDrone = saveDrone("summary-ready", DroneStatus.READY);
        saveDrone("summary-flying", DroneStatus.FLYING);
        saveDrone("summary-returning", DroneStatus.RETURNING);
        saveDrone("summary-charging", DroneStatus.CHARGING);
        saveDrone("summary-maintenance", DroneStatus.MAINTENANCE);
        saveDrone("summary-disabled", DroneStatus.DISABLED);

        DroneDispatch dispatched = saveDispatch(readyDrone, lv1, DroneDispatchStatus.DISPATCHED, "dispatched");
        saveDispatch(readyDrone, lv1, DroneDispatchStatus.ARRIVED, "arrived");
        saveDispatch(readyDrone, lv1, DroneDispatchStatus.KIT_DROPPING, "dropping");
        saveDispatch(readyDrone, lv1, DroneDispatchStatus.REQUESTED, "requested");

        saveDropLog(dispatched, readyDrone, normalWorker, lv1, DropStatus.DROPPED);
        saveDropLog(dispatched, readyDrone, normalWorker, lv1, DropStatus.DROPPED);
        saveDropLog(dispatched, readyDrone, normalWorker, lv1, DropStatus.FAILED);

        saveWearableCommand(normalWorker, wornEquipment, WearableCommandType.BUZZER_ON, WearableCommandStatus.REQUESTED);
        saveWearableCommand(normalWorker, wornEquipment, WearableCommandType.BUZZER_ON, WearableCommandStatus.SENT);
        saveWearableCommand(normalWorker, wornEquipment, WearableCommandType.BUZZER_ON, WearableCommandStatus.ACKNOWLEDGED);
        saveWearableCommand(normalWorker, wornEquipment, WearableCommandType.BUZZER_OFF, WearableCommandStatus.REQUESTED);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.totalWorkers").value(4))
                .andExpect(jsonPath("$.data.normalWorkers").value(1))
                .andExpect(jsonPath("$.data.warningWorkers").value(1))
                .andExpect(jsonPath("$.data.dangerWorkers").value(1))
                .andExpect(jsonPath("$.data.lv1RiskEvents").value(1))
                .andExpect(jsonPath("$.data.lv2RiskEvents").value(1))
                .andExpect(jsonPath("$.data.lv3RiskEvents").value(1))
                .andExpect(jsonPath("$.data.lv4RiskEvents").value(1))
                .andExpect(jsonPath("$.data.totalEquipment").value(3))
                .andExpect(jsonPath("$.data.wornEquipment").value(1))
                .andExpect(jsonPath("$.data.notWornEquipment").value(1))
                .andExpect(jsonPath("$.data.activeRiskEvents").value(3))
                .andExpect(jsonPath("$.data.unreadAlerts").value(2))
                .andExpect(jsonPath("$.data.activeDroneDispatches").value(3))
                .andExpect(jsonPath("$.data.emergencyKitDropped").value(2))
                .andExpect(jsonPath("$.data.activeBuzzerCommands").value(2))
                .andExpect(jsonPath("$.data.readyDrones").value(1))
                .andExpect(jsonPath("$.data.flyingDrones").value(1));
    }

    private Worker saveWorker(String key, WorkerStatus status) {
        return workerRepository.save(Worker.builder()
                .name(key)
                .department("safety")
                .phoneNumber("010-" + key)
                .rfidTag("rfid-" + key)
                .status(status)
                .build());
    }

    private Equipment saveEquipment(String key, WearStatus wearStatus, Worker worker) {
        return equipmentRepository.save(Equipment.builder()
                .worker(worker)
                .serialNumber("equipment-" + key)
                .name(key)
                .type(EquipmentType.HELMET)
                .status(worker == null ? EquipmentStatus.AVAILABLE : EquipmentStatus.ASSIGNED)
                .wearStatus(wearStatus)
                .build());
    }

    private Drone saveDrone(String key, DroneStatus status) {
        return droneRepository.save(Drone.builder()
                .name(key)
                .serialNumber("drone-" + key)
                .modelName("test-model")
                .status(status)
                .batteryPercent(80)
                .build());
    }

    private RiskEvent saveRiskEvent(
            Worker worker,
            RiskLevel riskLevel,
            RiskStatus status,
            LocalDateTime occurredAt
    ) {
        return riskEventRepository.save(RiskEvent.builder()
                .worker(worker)
                .sourceType(RiskSourceType.SENSOR)
                .riskType(RiskType.FALL_DETECTED)
                .riskLevel(riskLevel)
                .description("test risk event")
                .status(status)
                .occurredAt(occurredAt)
                .build());
    }

    private Alert saveAlert(
            String title,
            AlertReadStatus readStatus,
            Worker worker,
            RiskEvent riskEvent
    ) {
        return alertRepository.save(Alert.builder()
                .riskEvent(riskEvent)
                .worker(worker)
                .title(title)
                .message("test alert")
                .severity(AlertSeverity.WARNING)
                .readStatus(readStatus)
                .build());
    }

    private DroneDispatch saveDispatch(
            Drone drone,
            RiskEvent riskEvent,
            DroneDispatchStatus status,
            String key
    ) {
        return droneDispatchRepository.save(DroneDispatch.builder()
                .drone(drone)
                .riskEvent(riskEvent)
                .status(status)
                .commandMessage("command-" + key)
                .build());
    }

    private DroneDropLog saveDropLog(
            DroneDispatch dispatch,
            Drone drone,
            Worker worker,
            RiskEvent riskEvent,
            DropStatus status
    ) {
        return droneDropLogRepository.save(DroneDropLog.builder()
                .droneDispatch(dispatch)
                .drone(drone)
                .worker(worker)
                .riskEvent(riskEvent)
                .dropMethod(DropMethod.MANUAL)
                .dropStatus(status)
                .build());
    }

    private WearableCommand saveWearableCommand(
            Worker worker,
            Equipment equipment,
            WearableCommandType type,
            WearableCommandStatus status
    ) {
        return wearableCommandRepository.save(WearableCommand.builder()
                .worker(worker)
                .equipment(equipment)
                .commandType(type)
                .commandStatus(status)
                .reason("test command")
                .requestedAt(LocalDateTime.of(2026, 7, 15, 11, 0))
                .build());
    }
}
