package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.dto.request.DroneCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchCreateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneDispatchStatusUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneUpdateRequest;
import com.worksafe.backend.domain.drone.dto.request.DroneVideoCreateRequest;
import com.worksafe.backend.domain.drone.dto.response.DroneDispatchResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneResponse;
import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.service.DroneService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "드론 API", description = "드론 등록, 조회, 수정, 삭제, 출동, 영상 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drones")
public class DroneController {

    private final DroneService droneService;

    @PostMapping
    @Operation(summary = "드론 등록")
    public ApiResponse<DroneResponse> create(@Valid @RequestBody DroneCreateRequest request) {
        return ApiResponse.created(droneService.create(request));
    }

    @GetMapping
    @Operation(summary = "드론 목록 조회")
    public ApiResponse<List<DroneResponse>> findAll() {
        return ApiResponse.success(droneService.findAll());
    }

    @GetMapping("/{droneId}")
    @Operation(summary = "드론 상세 조회")
    public ApiResponse<DroneResponse> findById(@PathVariable Long droneId) {
        return ApiResponse.success(droneService.findById(droneId));
    }

    @PatchMapping("/{droneId}")
    @Operation(summary = "드론 정보 수정")
    public ApiResponse<DroneResponse> update(
            @PathVariable Long droneId,
            @Valid @RequestBody DroneUpdateRequest request
    ) {
        return ApiResponse.success(droneService.update(droneId, request));
    }

    @DeleteMapping("/{droneId}")
    @Operation(summary = "드론 삭제")
    public ApiResponse<Void> delete(@PathVariable Long droneId) {
        droneService.delete(droneId);
        return ApiResponse.success();
    }

    @PostMapping("/{droneId}/dispatch")
    @Operation(summary = "드론 출동 명령")
    public ApiResponse<DroneDispatchResponse> dispatch(
            @PathVariable Long droneId,
            @Valid @RequestBody DroneDispatchCreateRequest request
    ) {
        return ApiResponse.created(droneService.dispatch(droneId, request));
    }

    @GetMapping("/dispatches")
    @Operation(summary = "드론 출동 목록 조회")
    public ApiResponse<List<DroneDispatchResponse>> findAllDispatches() {
        return ApiResponse.success(droneService.findAllDispatches());
    }

    @GetMapping("/dispatches/{dispatchId}")
    @Operation(summary = "드론 출동 상세 조회")
    public ApiResponse<DroneDispatchResponse> findDispatchById(@PathVariable Long dispatchId) {
        return ApiResponse.success(droneService.findDispatchById(dispatchId));
    }

    @PatchMapping("/dispatches/{dispatchId}/status")
    @Operation(summary = "드론 출동 상태 변경")
    public ApiResponse<DroneDispatchResponse> updateDispatchStatus(
            @PathVariable Long dispatchId,
            @Valid @RequestBody DroneDispatchStatusUpdateRequest request
    ) {
        return ApiResponse.success(droneService.updateDispatchStatus(dispatchId, request));
    }

    @PostMapping("/{droneId}/videos")
    @Operation(summary = "드론 영상 송출 생성")
    public ApiResponse<DroneVideoResponse> createVideo(
            @PathVariable Long droneId,
            @Valid @RequestBody DroneVideoCreateRequest request
    ) {
        return ApiResponse.created(droneService.createVideo(droneId, request));
    }

    @GetMapping("/{droneId}/videos/active")
    @Operation(summary = "활성 드론 영상 목록 조회")
    public ApiResponse<DroneVideoResponse> findActiveVideo(@PathVariable Long droneId) {
        return ApiResponse.success(droneService.findActiveVideo(droneId));
    }
}
