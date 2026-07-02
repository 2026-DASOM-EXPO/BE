package com.worksafe.backend.domain.risk.controller;

import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.request.RiskEventStatusUpdateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.service.RiskService;
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

@Tag(name = "위험 이벤트 API", description = "위험 이벤트 생성, 목록 조회, 단건 조회, 상태 수정, 작업자별 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk-events")
public class RiskEventController {

    private final RiskService riskService;

    @PostMapping
    @Operation(summary = "위험 이벤트 생성")
    public ApiResponse<RiskEventResponse> create(@Valid @RequestBody RiskEventCreateRequest request) {
        return ApiResponse.created(riskService.create(request));
    }

    @GetMapping
    @Operation(summary = "위험 이벤트 목록 조회")
    public ApiResponse<List<RiskEventResponse>> findAll() {
        return ApiResponse.success(riskService.findAll());
    }

    @GetMapping("/{riskEventId}")
    @Operation(summary = "위험 이벤트 단건 조회")
    public ApiResponse<RiskEventResponse> findById(@PathVariable Long riskEventId) {
        return ApiResponse.success(riskService.findById(riskEventId));
    }

    @PatchMapping("/{riskEventId}/status")
    @Operation(summary = "위험 이벤트 상태 수정")
    public ApiResponse<RiskEventResponse> updateStatus(
            @PathVariable Long riskEventId,
            @Valid @RequestBody RiskEventStatusUpdateRequest request
    ) {
        return ApiResponse.success(riskService.updateStatus(riskEventId, request));
    }

    @GetMapping("/workers/{workerId}")
    @Operation(summary = "작업자별 위험 이벤트 목록 조회")
    public ApiResponse<List<RiskEventResponse>> findByWorkerId(@PathVariable Long workerId) {
        return ApiResponse.success(riskService.findByWorkerId(workerId));
    }
}
