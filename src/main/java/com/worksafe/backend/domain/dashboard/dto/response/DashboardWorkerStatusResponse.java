package com.worksafe.backend.domain.dashboard.dto.response;

public record DashboardWorkerStatusResponse(
        long totalWorkers,
        long normalWorkers,
        long warningWorkers,
        long dangerWorkers,
        long inactiveWorkers
) {
}
