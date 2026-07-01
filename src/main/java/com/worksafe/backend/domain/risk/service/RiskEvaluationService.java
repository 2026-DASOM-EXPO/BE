package com.worksafe.backend.domain.risk.service;

import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.risk.enums.RiskLevel;

public interface RiskEvaluationService {

    RiskLevel evaluateWorkerRisk(Long workerId);

    RiskEventResponse evaluateBySensorLog(SensorLog sensorLog);

    RiskEventResponse evaluateByEquipmentStatus(Long workerId);

    RiskEventResponse evaluateBySos(Long workerId);

    void handleRiskEvent(RiskEvent riskEvent);
}
