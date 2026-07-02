package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.dto.request.DroneDropLogCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDropLogStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDropLogResponse;
import com.worksafe.backend.domain.drone.service.DroneDropLogService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "드론 투하 로그 API", description = "응급키트 투하 로그 생성, 조회, 상태 변경")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DroneDropLogController {

    private final DroneDropLogService droneDropLogService;

    @PostMapping("/drone-dispatches/{dispatchId}/drop-logs")
    @Operation(summary = "응급키트 투하 로그 생성")
    public ApiResponse<DroneDropLogResponse> create(
            @PathVariable Long dispatchId,
            @Valid @RequestBody DroneDropLogCreateRequest request
    ) {
        return ApiResponse.created(droneDropLogService.create(dispatchId, request));
    }

    @GetMapping("/drone-dispatches/{dispatchId}/drop-logs")
    @Operation(summary = "응급키트 투하 로그 목록 조회")
    public ApiResponse<List<DroneDropLogResponse>> findByDispatchId(@PathVariable Long dispatchId) {
        return ApiResponse.success(droneDropLogService.findByDispatchId(dispatchId));
    }

    @PatchMapping("/drone-drop-logs/{dropLogId}/status")
    @Operation(summary = "응급키트 투하 상태 변경")
    public ApiResponse<DroneDropLogResponse> updateStatus(
            @PathVariable Long dropLogId,
            @Valid @RequestBody DroneDropLogStatusUpdateRequest request
    ) {
        return ApiResponse.success(droneDropLogService.updateStatus(dropLogId, request));
    }
}
