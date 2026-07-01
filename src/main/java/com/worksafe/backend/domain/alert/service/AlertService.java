package com.worksafe.backend.domain.alert.service;

import com.worksafe.backend.domain.alert.dto.response.AlertResponse;

import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AlertService {

    List<AlertResponse> findAll();

    List<AlertResponse> findUnread();

    AlertResponse markAsRead(Long alertId);

    void markAllAsRead();

    SseEmitter stream();
}
