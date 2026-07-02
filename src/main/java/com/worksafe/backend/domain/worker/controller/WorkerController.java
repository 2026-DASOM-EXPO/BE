package com.worksafe.backend.domain.worker.controller;

import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.domain.worker.dto.request.WorkerCreateRequest;
import com.worksafe.backend.domain.worker.dto.request.WorkerUpdateRequest;
import com.worksafe.backend.domain.worker.dto.response.WorkerResponse;
import com.worksafe.backend.domain.worker.service.WorkerService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "작업자 API", description = "작업자 등록, 목록 조회, 단건 조회, 수정, 위치 갱신, 삭제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping
    @Operation(summary = "작업자 등록")
    public ApiResponse<WorkerResponse> create(@Valid @RequestBody WorkerCreateRequest request) {
        return ApiResponse.created(workerService.create(request));
    }

    @GetMapping
    @Operation(summary = "작업자 목록 조회")
    public ApiResponse<List<WorkerResponse>> findAll() {
        return ApiResponse.success(workerService.findAll());
    }

    @GetMapping("/{workerId}")
    @Operation(summary = "작업자 상세 조회")
    public ApiResponse<WorkerResponse> findById(@PathVariable Long workerId) {
        return ApiResponse.success(workerService.findById(workerId));
    }

    @PatchMapping("/{workerId}")
    @Operation(summary = "작업자 수정")
    public ApiResponse<WorkerResponse> update(
            @PathVariable Long workerId,
            @Valid @RequestBody WorkerUpdateRequest request
    ) {
        return ApiResponse.success(workerService.update(workerId, request));
    }

    @PatchMapping("/{workerId}/location")
    @Operation(summary = "작업자 위치 갱신")
    public ApiResponse<WorkerResponse> updateLocation(
            @PathVariable Long workerId,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return ApiResponse.success(workerService.updateLocation(workerId, latitude, longitude));
    }

    @DeleteMapping("/{workerId}")
    @Operation(summary = "작업자 삭제")
    public ApiResponse<Void> delete(@PathVariable Long workerId) {
        workerService.delete(workerId);
        return ApiResponse.success();
    }
}
