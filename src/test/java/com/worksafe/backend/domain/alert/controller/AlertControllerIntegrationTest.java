package com.worksafe.backend.domain.alert.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskSourceType;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private RiskEventRepository riskEventRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAllReturnsLatestThirtyInStableOrderWithRelationsUsingOneQuery() throws Exception {
        LocalDateTime latestCreatedAt = LocalDateTime.of(2026, 7, 16, 12, 0);
        List<Alert> alerts = new ArrayList<>();

        for (int index = 0; index < 35; index++) {
            Worker worker = saveWorker("all-" + index);
            RiskEvent riskEvent = saveRiskEvent("all-" + index, worker);
            Alert alert = saveAlert(
                    "all-" + index,
                    index % 2 == 0 ? AlertReadStatus.UNREAD : AlertReadStatus.READ,
                    null,
                    worker,
                    riskEvent
            );
            alerts.add(alert);
            setCreatedAt(alert, index < 2 ? latestCreatedAt : latestCreatedAt.minusMinutes(index - 1L));
        }
        entityManager.clear();

        MvcResult result = performGetWithSingleSelect("/api/alerts");
        JsonNode data = responseData(result);

        assertThat(data).hasSize(30);
        assertThat(responseIds(data)).containsExactlyElementsOf(expectedTopThirtyIds(alerts));
        assertRelations(data.get(0));
    }

    @Test
    void findUnreadReturnsLatestThirtyUnreadInStableOrderUsingOneQuery() throws Exception {
        LocalDateTime latestCreatedAt = LocalDateTime.of(2026, 7, 16, 12, 0);
        List<Alert> unreadAlerts = new ArrayList<>();

        for (int index = 0; index < 35; index++) {
            Worker worker = saveWorker("unread-" + index);
            RiskEvent riskEvent = saveRiskEvent("unread-" + index, worker);
            Alert alert = saveAlert(
                    "unread-" + index,
                    AlertReadStatus.UNREAD,
                    null,
                    worker,
                    riskEvent
            );
            unreadAlerts.add(alert);
            setCreatedAt(alert, index < 2 ? latestCreatedAt : latestCreatedAt.minusMinutes(index - 1L));
        }

        Alert readAlert = saveAlert("newer-read", AlertReadStatus.READ, latestCreatedAt.plusHours(1));
        setCreatedAt(readAlert, latestCreatedAt.plusHours(1));
        entityManager.clear();

        MvcResult result = performGetWithSingleSelect("/api/alerts/unread");
        JsonNode data = responseData(result);

        assertThat(data).hasSize(30);
        assertThat(responseIds(data)).containsExactlyElementsOf(expectedTopThirtyIds(unreadAlerts));
        data.forEach(alert -> assertThat(alert.get("readStatus").asText()).isEqualTo("UNREAD"));
        assertThat(responseIds(data)).doesNotContain(readAlert.getId());
        assertRelations(data.get(0));
    }

    @Test
    void markAllAsReadUpdatesEveryUnreadAlertAndPreservesExistingReadAlert() throws Exception {
        Alert firstUnread = saveAlert("first-unread", AlertReadStatus.UNREAD, null);
        Alert secondUnread = saveAlert("second-unread", AlertReadStatus.UNREAD, null);
        LocalDateTime existingReadAt = LocalDateTime.of(2026, 7, 1, 9, 0);
        Alert existingRead = saveAlert("existing-read", AlertReadStatus.READ, existingReadAt);
        alertRepository.flush();

        mockMvc.perform(patch("/api/alerts/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("성공"));

        Alert updatedFirst = alertRepository.findById(firstUnread.getId()).orElseThrow();
        Alert updatedSecond = alertRepository.findById(secondUnread.getId()).orElseThrow();
        Alert unchangedRead = alertRepository.findById(existingRead.getId()).orElseThrow();

        assertThat(updatedFirst.getReadStatus()).isEqualTo(AlertReadStatus.READ);
        assertThat(updatedSecond.getReadStatus()).isEqualTo(AlertReadStatus.READ);
        assertThat(updatedFirst.getReadAt()).isNotNull();
        assertThat(updatedSecond.getReadAt()).isEqualTo(updatedFirst.getReadAt());
        assertThat(unchangedRead.getReadStatus()).isEqualTo(AlertReadStatus.READ);
        assertThat(unchangedRead.getReadAt()).isEqualTo(existingReadAt);
        assertThat(alertRepository.countByReadStatus(AlertReadStatus.UNREAD)).isZero();
    }

    @Test
    void markAllAsReadSucceedsWhenThereAreNoUnreadAlerts() throws Exception {
        LocalDateTime existingReadAt = LocalDateTime.of(2026, 7, 1, 9, 0);
        Alert existingRead = saveAlert("already-read", AlertReadStatus.READ, existingReadAt);
        alertRepository.flush();

        mockMvc.perform(patch("/api/alerts/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("성공"));

        Alert unchangedRead = alertRepository.findById(existingRead.getId()).orElseThrow();
        assertThat(unchangedRead.getReadStatus()).isEqualTo(AlertReadStatus.READ);
        assertThat(unchangedRead.getReadAt()).isEqualTo(existingReadAt);
    }

    private Alert saveAlert(String title, AlertReadStatus readStatus, LocalDateTime readAt) {
        return saveAlert(title, readStatus, readAt, null, null);
    }

    private Alert saveAlert(
            String title,
            AlertReadStatus readStatus,
            LocalDateTime readAt,
            Worker worker,
            RiskEvent riskEvent
    ) {
        return alertRepository.save(Alert.builder()
                .worker(worker)
                .riskEvent(riskEvent)
                .title(title)
                .message("test alert")
                .severity(AlertSeverity.WARNING)
                .readStatus(readStatus)
                .readAt(readAt)
                .build());
    }

    private Worker saveWorker(String key) {
        return workerRepository.save(Worker.builder()
                .name("worker-" + key)
                .department("safety")
                .phoneNumber("010-" + key)
                .rfidTag("rfid-" + key)
                .status(WorkerStatus.NORMAL)
                .build());
    }

    private RiskEvent saveRiskEvent(String key, Worker worker) {
        return riskEventRepository.save(RiskEvent.builder()
                .worker(worker)
                .sourceType(RiskSourceType.SENSOR)
                .riskType(RiskType.FALL_DETECTED)
                .riskLevel(RiskLevel.LV2)
                .description("risk-" + key)
                .status(RiskStatus.OPEN)
                .occurredAt(LocalDateTime.of(2026, 7, 16, 10, 0))
                .build());
    }

    private void setCreatedAt(Alert alert, LocalDateTime createdAt) {
        alertRepository.flush();
        jdbcTemplate.update(
                "UPDATE alerts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt),
                alert.getId()
        );
    }

    private MvcResult performGetWithSingleSelect(String path) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        MvcResult result;
        long queryCount;
        try {
            result = mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andReturn();
            queryCount = statistics.getPrepareStatementCount();
        } finally {
            statistics.setStatisticsEnabled(false);
        }

        assertThat(queryCount).isEqualTo(1);
        return result;
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("data");
    }

    private List<Long> responseIds(JsonNode data) {
        List<Long> ids = new ArrayList<>();
        data.forEach(alert -> ids.add(alert.get("id").asLong()));
        return ids;
    }

    private List<Long> expectedTopThirtyIds(List<Alert> alerts) {
        List<Long> ids = new ArrayList<>();
        ids.add(alerts.get(1).getId());
        ids.add(alerts.get(0).getId());
        alerts.subList(2, 30).forEach(alert -> ids.add(alert.getId()));
        return ids;
    }

    private void assertRelations(JsonNode alert) {
        assertThat(alert.get("worker").get("id").asLong()).isPositive();
        assertThat(alert.get("riskEvent").get("id").asLong()).isPositive();
        assertThat(alert.get("riskEvent").get("worker").get("id").asLong()).isPositive();
    }
}
