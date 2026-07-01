package com.worksafe.backend.domain.equipment.repository;

import com.worksafe.backend.domain.equipment.entity.WearableCommand;
import com.worksafe.backend.domain.equipment.enums.WearableCommandStatus;
import com.worksafe.backend.domain.equipment.enums.WearableCommandType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface WearableCommandRepository extends JpaRepository<WearableCommand, Long> {

    List<WearableCommand> findByCommandStatusInOrderByCreatedAtAsc(Collection<WearableCommandStatus> commandStatuses);

    List<WearableCommand> findByWorker_IdOrderByCreatedAtDesc(Long workerId);

    long countByCommandTypeInAndCommandStatusIn(Collection<WearableCommandType> commandTypes, Collection<WearableCommandStatus> commandStatuses);
}
