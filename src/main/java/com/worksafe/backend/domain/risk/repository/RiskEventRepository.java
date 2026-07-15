package com.worksafe.backend.domain.risk.repository;

import com.worksafe.backend.domain.risk.entity.RiskEvent;
import com.worksafe.backend.domain.risk.enums.RiskLevel;
import com.worksafe.backend.domain.risk.enums.RiskStatus;
import com.worksafe.backend.domain.risk.enums.RiskType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {

    List<RiskEvent> findByWorker_IdOrderByOccurredAtDesc(Long workerId);

    List<RiskEvent> findAllByOrderByOccurredAtDesc();

    @EntityGraph(attributePaths = "worker")
    List<RiskEvent> findTop10ByStatusInOrderByOccurredAtDescIdDesc(Collection<RiskStatus> statuses);

    List<RiskEvent> findByWorker_IdAndRiskLevelAndStatusOrderByOccurredAtDesc(Long workerId, RiskLevel riskLevel, RiskStatus status);

    long countByStatusNot(RiskStatus status);

    long countByRiskLevel(RiskLevel riskLevel);

    boolean existsByWorker_IdAndRiskTypeAndStatusIn(Long workerId, RiskType riskType, Collection<RiskStatus> statuses);

    RiskEvent findFirstByWorker_IdAndRiskTypeAndStatusInOrderByOccurredAtDesc(Long workerId, RiskType riskType, Collection<RiskStatus> statuses);

    List<RiskEvent> findByWorker_IdAndStatusInOrderByOccurredAtDesc(Long workerId, Collection<RiskStatus> statuses);
}
