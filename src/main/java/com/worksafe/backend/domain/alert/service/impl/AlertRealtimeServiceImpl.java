package com.worksafe.backend.domain.alert.service.impl;

import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class AlertRealtimeServiceImpl implements AlertRealtimeService {

    private final AlertRepository alertRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((error) -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data(AlertConverter.toResponseList(alertRepository.findByReadStatusOrderByCreatedAtDesc(AlertReadStatus.UNREAD))));
        } catch (IOException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Override
    public void publish(AlertResponse alertResponse) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(alertResponse);
                }
            });
            return;
        }

        send(alertResponse);
    }

    private void send(AlertResponse alertResponse) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("alert").data(alertResponse));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
