package com.worksafe.backend.domain.alert.service.impl;

import com.worksafe.backend.domain.alert.converter.AlertConverter;
import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import com.worksafe.backend.domain.alert.entity.Alert;
import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertService;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertRealtimeService alertRealtimeService;

    @Override
    public List<AlertResponse> findAll() {
        return AlertConverter.toResponseList(alertRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<AlertResponse> findUnread() {
        return AlertConverter.toResponseList(alertRepository.findByReadStatusOrderByCreatedAtDesc(AlertReadStatus.UNREAD));
    }

    @Override
    public AlertResponse markAsRead(Long alertId) {
        Alert alert = getAlert(alertId);
        alert.markAsRead();
        return AlertConverter.toResponse(alert);
    }

    @Override
    public void markAllAsRead() {
        alertRepository.markAllAsRead(
                AlertReadStatus.UNREAD,
                AlertReadStatus.READ,
                LocalDateTime.now()
        );
    }

    @Override
    public SseEmitter stream() {
        return alertRealtimeService.register();
    }

    private Alert getAlert(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
    }
}
