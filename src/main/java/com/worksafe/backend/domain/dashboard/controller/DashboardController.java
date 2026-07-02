package com.worksafe.backend.domain.dashboard.controller;

import com.worksafe.backend.domain.dashboard.dto.response.DashboardDroneStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardEquipmentStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardSummaryResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardWorkerStatusResponse;
import com.worksafe.backend.domain.dashboard.service.DashboardService;
import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "대시보드 요약 조회")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.success(dashboardService.summary());
    }

    @GetMapping("/workers/status")
    @Operation(summary = "작업자 상태 조회")
    public ApiResponse<DashboardWorkerStatusResponse> workerStatus() {
        return ApiResponse.success(dashboardService.workerStatus());
    }

    @GetMapping("/equipment/status")
    @Operation(summary = "장비 상태 조회")
    public ApiResponse<DashboardEquipmentStatusResponse> equipmentStatus() {
        return ApiResponse.success(dashboardService.equipmentStatus());
    }

    @GetMapping("/risk-events/recent")
    @Operation(summary = "최근 위험 이벤트 조회")
    public ApiResponse<List<RiskEventResponse>> recentRiskEvents() {
        return ApiResponse.success(dashboardService.recentRiskEvents());
    }

    @GetMapping("/drones/status")
    @Operation(summary = "드론 상태 조회")
    public ApiResponse<DashboardDroneStatusResponse> droneStatus() {
        return ApiResponse.success(dashboardService.droneStatus());
    }
}
