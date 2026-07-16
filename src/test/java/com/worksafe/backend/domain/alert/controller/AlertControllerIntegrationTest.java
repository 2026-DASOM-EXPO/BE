package com.worksafe.backend.domain.alert.controller;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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
    private AlertRepository alertRepository;

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
        return alertRepository.save(Alert.builder()
                .title(title)
                .message("test alert")
                .severity(AlertSeverity.WARNING)
                .readStatus(readStatus)
                .readAt(readAt)
                .build());
    }
}
