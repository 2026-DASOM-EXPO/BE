package com.worksafe.backend.domain.dashboard.service;

import com.worksafe.backend.domain.dashboard.dto.response.DashboardDroneStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardEquipmentStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardSummaryResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardWorkerStatusResponse;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse summary();

    DashboardWorkerStatusResponse workerStatus();

    DashboardEquipmentStatusResponse equipmentStatus();

    List<RiskEventResponse> recentRiskEvents();

    DashboardDroneStatusResponse droneStatus();
}
