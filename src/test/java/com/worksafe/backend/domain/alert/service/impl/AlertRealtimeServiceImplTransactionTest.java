package com.worksafe.backend.domain.alert.service.impl;

import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.enums.AlertSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class AlertRealtimeServiceImplTransactionTest {

    @Autowired
    private AlertRealtimeServiceImpl alertRealtimeService;

    @AfterEach
    void clearEmitters() {
        emitters().clear();
    }

    @Test
    @Transactional
    void publishesOnlyAfterTransactionCommit() throws IOException {
        SseEmitter emitter = addEmitter();

        alertRealtimeService.publish(alertResponse());

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @Transactional
    void doesNotPublishWhenTransactionRollsBack() throws IOException {
        SseEmitter emitter = addEmitter();

        alertRealtimeService.publish(alertResponse());

        TestTransaction.flagForRollback();
        TestTransaction.end();

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void publishesImmediatelyWithoutTransaction() throws IOException {
        SseEmitter emitter = addEmitter();

        alertRealtimeService.publish(alertResponse());

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    private SseEmitter addEmitter() {
        SseEmitter emitter = mock(SseEmitter.class);
        emitters().add(emitter);
        return emitter;
    }

    @SuppressWarnings("unchecked")
    private List<SseEmitter> emitters() {
        return (List<SseEmitter>) ReflectionTestUtils.getField(alertRealtimeService, "emitters");
    }

    private AlertResponse alertResponse() {
        return new AlertResponse(
                1L,
                null,
                null,
                "test alert",
                "test message",
                AlertSeverity.WARNING,
                AlertReadStatus.UNREAD,
                LocalDateTime.of(2026, 7, 16, 10, 0),
                null
        );
    }
}
