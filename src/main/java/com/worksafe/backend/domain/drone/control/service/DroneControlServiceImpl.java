package com.worksafe.backend.domain.drone.control.service;

import com.worksafe.backend.domain.drone.control.dto.DroneControlRequest;
import com.worksafe.backend.domain.drone.control.dto.DroneControlResponse;
import com.worksafe.backend.domain.drone.control.port.JetsonCommand;
import com.worksafe.backend.domain.drone.control.port.JetsonCommandPort;
import com.worksafe.backend.domain.drone.control.port.JetsonDeliveryResult;
import com.worksafe.backend.domain.drone.entity.Drone;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DroneControlServiceImpl implements DroneControlService {

    static final int DEFAULT_DURATION_SECONDS = 1;

    private final DroneRepository droneRepository;
    private final JetsonCommandPort jetsonCommandPort;

    @Override
    public DroneControlResponse send(Long droneId, DroneControlRequest request) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRONE_NOT_FOUND));
        int durationSeconds = request.durationSeconds() == null
                ? DEFAULT_DURATION_SECONDS
                : request.durationSeconds();

        JetsonCommand command = new JetsonCommand(
                UUID.randomUUID(),
                droneId,
                drone.getSerialNumber(),
                request.command(),
                durationSeconds,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        JetsonDeliveryResult deliveryResult = jetsonCommandPort.send(command);

        return new DroneControlResponse(
                command.commandId(),
                command.droneId(),
                command.droneSerialNumber(),
                command.command(),
                command.durationSeconds(),
                command.issuedAt(),
                deliveryResult.status(),
                deliveryResult.detail()
        );
    }
}
