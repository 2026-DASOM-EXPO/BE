package com.worksafe.backend.domain.risk.controller;

import com.worksafe.backend.global.common.response.ApiResponse;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventReportResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Risk Event API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events/risk")
public class RiskEventApiController {

    private final RiskService riskService;

    @PostMapping
    @Operation(summary = "위험 이벤트 생성")
    public ApiResponse<RiskEventResponse> create(@Valid @RequestBody RiskEventCreateRequest request) {
        return ApiResponse.created(riskService.create(request));
    }

    @GetMapping
    @Operation(summary = "위험 이벤트 및 드론 영상 리포트 조회")
    public ApiResponse<List<RiskEventReportResponse>> findReports(
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) RiskStatus status,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        return ApiResponse.success(riskService.findReports(workerId, riskLevel, status, from, to));
    }
}
