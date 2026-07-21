package com.worksafe.backend.domain.dashboard.service.impl;

import com.worksafe.backend.domain.alert.enums.AlertReadStatus;
import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardDroneStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardEquipmentStatusResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardSummaryResponse;
import com.worksafe.backend.domain.dashboard.dto.response.DashboardWorkerStatusResponse;
import com.worksafe.backend.domain.dashboard.service.DashboardService;
import com.worksafe.backend.domain.drone.enums.DroneDispatchStatus;
import com.worksafe.backend.domain.drone.enums.DroneStatus;
import com.worksafe.backend.domain.drone.enums.DropStatus;
import com.worksafe.backend.domain.drone.repository.DroneDropLogRepository;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.equipment.enums.WearStatus;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import com.worksafe.backend.domain.equipment.repository.WearableCommandRepository;
import com.worksafe.backend.domain.risk.converter.RiskEventConverter;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.worker.enums.WorkerStatus;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private static final List<RiskStatus> ACTIVE_RISK_STATUSES = List.of(
            RiskStatus.OPEN,
            RiskStatus.PROCESSING
    );

    private final WorkerRepository workerRepository;
    private final EquipmentRepository equipmentRepository;
    private final RiskEventRepository riskEventRepository;
    private final AlertRepository alertRepository;
    private final DroneDispatchRepository droneDispatchRepository;
    private final DroneDropLogRepository droneDropLogRepository;
    private final WearableCommandRepository wearableCommandRepository;
    private final DroneRepository droneRepository;

    @Override
    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                workerRepository.count(),
                workerRepository.countByStatus(WorkerStatus.NORMAL),
                workerRepository.countByStatus(WorkerStatus.WARNING),
                workerRepository.countByStatus(WorkerStatus.DANGER),
                riskEventRepository.countByRiskLevel(RiskLevel.LV1),
                riskEventRepository.countByRiskLevel(RiskLevel.LV2),
                riskEventRepository.countByRiskLevel(RiskLevel.LV3),
                riskEventRepository.countByRiskLevel(RiskLevel.LV4),
                equipmentRepository.count(),
                equipmentRepository.countByWearStatus(WearStatus.WORN),
                equipmentRepository.countByWearStatus(WearStatus.NOT_WORN),
                riskEventRepository.countByStatusNot(RiskStatus.RESOLVED),
                alertRepository.countByReadStatus(AlertReadStatus.UNREAD),
                droneDispatchRepository.countByStatusIn(Set.of(DroneDispatchStatus.DISPATCHED, DroneDispatchStatus.ARRIVED, DroneDispatchStatus.KIT_DROPPING)),
                droneDropLogRepository.countByDropStatus(DropStatus.DROPPED),
                wearableCommandRepository.countByCommandTypeInAndCommandStatusIn(
                        List.of(WearableCommandType.BUZZER_ON),
                        List.of(WearableCommandStatus.REQUESTED, WearableCommandStatus.SENT)
                ),
                droneRepository.countByStatus(DroneStatus.READY),
                droneRepository.countByStatus(DroneStatus.FLYING)
        );
    }

    @Override
    public DashboardWorkerStatusResponse workerStatus() {
        return new DashboardWorkerStatusResponse(
                workerRepository.count(),
                workerRepository.countByStatus(WorkerStatus.NORMAL),
                workerRepository.countByStatus(WorkerStatus.WARNING),
                workerRepository.countByStatus(WorkerStatus.DANGER),
                workerRepository.countByStatus(WorkerStatus.INACTIVE)
        );
    }

    @Override
    public DashboardEquipmentStatusResponse equipmentStatus() {
        return new DashboardEquipmentStatusResponse(
                equipmentRepository.count(),
                equipmentRepository.countByWearStatus(WearStatus.WORN),
                equipmentRepository.countByWearStatus(WearStatus.NOT_WORN),
                equipmentRepository.countByWearStatus(WearStatus.UNKNOWN)
        );
    }

    @Override
    public List<RiskEventResponse> recentRiskEvents() {
        return RiskEventConverter.toResponseList(
                riskEventRepository.findTop10ByStatusInOrderByOccurredAtDescIdDesc(ACTIVE_RISK_STATUSES)
        );
    }

    @Override
    public DashboardDroneStatusResponse droneStatus() {
        return new DashboardDroneStatusResponse(
                droneRepository.countByStatus(DroneStatus.READY),
                droneRepository.countByStatus(DroneStatus.FLYING),
                droneRepository.countByStatus(DroneStatus.RETURNING),
                droneRepository.countByStatus(DroneStatus.CHARGING),
                droneRepository.countByStatus(DroneStatus.MAINTENANCE),
                droneRepository.countByStatus(DroneStatus.DISABLED)
        );
    }
}
