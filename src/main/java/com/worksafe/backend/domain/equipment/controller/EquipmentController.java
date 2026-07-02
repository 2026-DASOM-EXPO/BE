package com.worksafe.backend.domain.equipment.controller;

import com.worksafe.backend.domain.equipment.dto.request.EquipmentCreateRequest;
import com.worksafe.backend.domain.equipment.dto.request.BuzzerControlRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentUpdateRequest;
import com.worksafe.backend.domain.equipment.dto.request.EquipmentWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.request.ManualWearStatusRequest;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentLogResponse;
import com.worksafe.backend.domain.equipment.dto.request.WorkTimerRequest;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentResponse;
import com.worksafe.backend.domain.equipment.dto.response.EquipmentStatusResponse;
import com.worksafe.backend.domain.equipment.service.EquipmentService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

@Tag(name = "안전장비 API", description = "안전장비 등록, 조회, 배정, 착용 상태, 불출/반납, 부저, 작업 타이머 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @Operation(summary = "안전장비 등록")
    public ApiResponse<EquipmentResponse> create(@Valid @RequestBody EquipmentCreateRequest request) {
        return ApiResponse.created(equipmentService.create(request));
    }

    @GetMapping
    @Operation(summary = "안전장비 목록 조회")
    public ApiResponse<List<EquipmentResponse>> findAll() {
        return ApiResponse.success(equipmentService.findAll());
    }

    @GetMapping("/status")
    @Operation(summary = "안전장비 착용 상태 및 불출/반납 조회")
    public ApiResponse<List<EquipmentStatusResponse>> findStatus() {
        return ApiResponse.success(equipmentService.findStatus());
    }

    @GetMapping("/logs")
    @Operation(summary = "안전장비 착용 이력 조회")
    public ApiResponse<List<EquipmentLogResponse>> findLogs(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        return ApiResponse.success(equipmentService.findLogs(workerId, equipmentId, from, to));
    }

    @GetMapping("/{equipmentId}")
    @Operation(summary = "안전장비 상세 조회")
    public ApiResponse<EquipmentResponse> findById(@PathVariable Long equipmentId) {
        return ApiResponse.success(equipmentService.findById(equipmentId));
    }

    @PatchMapping("/{equipmentId}")
    @Operation(summary = "안전장비 수정")
    public ApiResponse<EquipmentResponse> update(
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentUpdateRequest request
    ) {
        return ApiResponse.success(equipmentService.update(equipmentId, request));
    }

    @PatchMapping("/{equipmentId}/assign/{workerId}")
    @Operation(summary = "안전장비 배정")
    public ApiResponse<EquipmentResponse> assign(
            @PathVariable Long equipmentId,
            @PathVariable Long workerId
    ) {
        return ApiResponse.success(equipmentService.assign(equipmentId, workerId));
    }

    @PatchMapping("/{equipmentId}/wear-status")
    @Operation(summary = "안전장비 착용 상태 수정")
    public ApiResponse<EquipmentResponse> updateWearStatus(
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentWearStatusRequest request
    ) {
        return ApiResponse.success(equipmentService.updateWearStatus(equipmentId, request));
    }

    @PatchMapping("/{equipmentId}/manual-wear-status")
    @Operation(summary = "안전장비 수동 착용 상태 설정")
    public ApiResponse<EquipmentResponse> updateManualWearStatus(
            @PathVariable Long equipmentId,
            @Valid @RequestBody ManualWearStatusRequest request
    ) {
        return ApiResponse.success(equipmentService.updateManualWearStatus(equipmentId, request));
    }

    @PatchMapping("/{equipmentId}/buzzer")
    @Operation(summary = "부저 제어")
    public ApiResponse<EquipmentResponse> updateBuzzer(
            @PathVariable Long equipmentId,
            @Valid @RequestBody BuzzerControlRequest request
    ) {
        return ApiResponse.success(equipmentService.updateBuzzer(equipmentId, request));
    }

    @PatchMapping("/{equipmentId}/work-timer/start")
    @Operation(summary = "작업 타이머 시작")
    public ApiResponse<EquipmentResponse> startWorkTimer(
            @PathVariable Long equipmentId,
            @Valid @RequestBody WorkTimerRequest request
    ) {
        return ApiResponse.success(equipmentService.startWorkTimer(equipmentId, request));
    }

    @PatchMapping("/{equipmentId}/work-timer/stop")
    @Operation(summary = "작업 타이머 종료")
    public ApiResponse<EquipmentResponse> stopWorkTimer(
            @PathVariable Long equipmentId,
            @Valid @RequestBody WorkTimerRequest request
    ) {
        return ApiResponse.success(equipmentService.stopWorkTimer(equipmentId, request));
    }

    @DeleteMapping("/{equipmentId}")
    @Operation(summary = "안전장비 삭제")
    public ApiResponse<Void> delete(@PathVariable Long equipmentId) {
        equipmentService.delete(equipmentId);
        return ApiResponse.success();
    }
}
