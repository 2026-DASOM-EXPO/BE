package com.worksafe.backend.domain.alert.service;

import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AlertRealtimeService {

    SseEmitter register();

    void publish(AlertResponse alertResponse);
}
