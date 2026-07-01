package com.worksafe.backend.domain.risk.service;

import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.dto.request.RiskEventStatusUpdateRequest;
import com.worksafe.backend.domain.risk.dto.response.RiskEventReportResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;

import java.util.List;
import java.time.LocalDateTime;

public interface RiskService {

    RiskEventResponse create(RiskEventCreateRequest request);

    List<RiskEventResponse> findAll();

    RiskEventResponse findById(Long riskEventId);

    RiskEventResponse updateStatus(Long riskEventId, RiskEventStatusUpdateRequest request);

    List<RiskEventResponse> findByWorkerId(Long workerId);

    List<RiskEventReportResponse> findReports(Long workerId, RiskLevel riskLevel, RiskStatus status, LocalDateTime from, LocalDateTime to);
}
