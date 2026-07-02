package com.worksafe.backend.domain.alert.controller;

import com.worksafe.backend.domain.alert.dto.response.AlertResponse;
import com.worksafe.backend.domain.alert.service.AlertService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "알림 API", description = "실시간 알림 목록 조회, 읽음 처리, SSE 스트림 연결")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "알림 목록 조회")
    public ApiResponse<List<AlertResponse>> findAll() {
        return ApiResponse.success(alertService.findAll());
    }

    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 알림 목록 조회")
    public ApiResponse<List<AlertResponse>> findUnread() {
        return ApiResponse.success(alertService.findUnread());
    }

    @PatchMapping("/{alertId}/read")
    @Operation(summary = "알림 읽음 상태 변경")
    public ApiResponse<AlertResponse> markAsRead(@PathVariable Long alertId) {
        return ApiResponse.success(alertService.markAsRead(alertId));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "전체 알림 읽음 처리")
    public ApiResponse<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return ApiResponse.success();
    }

    @GetMapping("/stream")
    @Operation(summary = "실시간 알림 SSE 스트림 연결")
    public SseEmitter stream() {
        return alertService.stream();
    }
}
