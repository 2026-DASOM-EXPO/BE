package com.worksafe.backend.domain.alert.service.impl;

import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertRealtimeServiceImplRegisterTest {

    @Test
    void registerUsesLatestThirtyUnreadAlertsForConnectedEvent() {
        AlertRepository alertRepository = mock(AlertRepository.class);
        Alert alert = Alert.builder()
                .title("test alert")
                .message("test message")
                .severity(AlertSeverity.WARNING)
                .readStatus(AlertReadStatus.UNREAD)
                .build();

        when(alertRepository.findTop30ByReadStatusOrderByCreatedAtDescIdDesc(AlertReadStatus.UNREAD))
                .thenReturn(List.of(alert));

        AlertRealtimeServiceImpl service = new AlertRealtimeServiceImpl(alertRepository);

        SseEmitter emitter = service.register();

        assertThat(emitter).isNotNull();
        verify(alertRepository)
                .findTop30ByReadStatusOrderByCreatedAtDescIdDesc(AlertReadStatus.UNREAD);
        verify(alertRepository, never())
                .findByReadStatusOrderByCreatedAtDesc(any(AlertReadStatus.class));

        emitter.complete();
    }
}
