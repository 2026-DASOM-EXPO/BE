package com.worksafe.backend.domain.sensor.controller;

import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.domain.sensor.dto.request.SensorLogCreateRequest;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.domain.sensor.service.SensorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "센서 로그 API", description = "센서 로그 생성, 전체 조회, 작업자/장비별 조회, 최신 로그 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sensor-logs")
public class SensorLogController {

    private final SensorLogService sensorLogService;

    @PostMapping
    @Operation(summary = "센서 로그 생성")
    public ApiResponse<SensorLogResponse> create(@Valid @RequestBody SensorLogCreateRequest request) {
        return ApiResponse.created(sensorLogService.create(request));
    }

    @GetMapping
    @Operation(summary = "센서 로그 전체 조회")
    public ApiResponse<List<SensorLogResponse>> findAll() {
        return ApiResponse.success(sensorLogService.findAll());
    }

    @GetMapping("/workers/{workerId}")
    @Operation(summary = "작업자별 센서 로그 조회")
    public ApiResponse<List<SensorLogResponse>> findByWorkerId(@PathVariable Long workerId) {
        return ApiResponse.success(sensorLogService.findByWorkerId(workerId));
    }

    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "장비별 센서 로그 조회")
    public ApiResponse<List<SensorLogResponse>> findByEquipmentId(@PathVariable Long equipmentId) {
        return ApiResponse.success(sensorLogService.findByEquipmentId(equipmentId));
    }

    @GetMapping("/latest/workers/{workerId}")
    @Operation(summary = "작업자 최신 센서 로그 조회")
    public ApiResponse<SensorLogResponse> findLatestByWorkerId(@PathVariable Long workerId) {
        return ApiResponse.success(sensorLogService.findLatestByWorkerId(workerId));
    }
}
