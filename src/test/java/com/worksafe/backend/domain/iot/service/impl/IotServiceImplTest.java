package com.worksafe.backend.domain.iot.service.impl;

import com.worksafe.backend.domain.alert.repository.AlertRepository;
import com.worksafe.backend.domain.alert.service.AlertRealtimeService;
import com.worksafe.backend.domain.drone.repository.DroneDispatchRepository;
import com.worksafe.backend.domain.drone.repository.DroneRepository;
import com.worksafe.backend.domain.equipment.repository.EquipmentRepository;
import com.worksafe.backend.domain.iot.dto.request.SosRequest;
import com.worksafe.backend.domain.risk.dto.request.RiskEventCreateRequest;
import com.worksafe.backend.domain.risk.repository.RiskEventRepository;
import com.worksafe.backend.domain.risk.service.RiskEvaluationService;
import com.worksafe.backend.domain.risk.service.RiskService;
import com.worksafe.backend.domain.sensor.entity.SensorLog;
import com.worksafe.backend.domain.sensor.repository.SensorLogRepository;
import com.worksafe.backend.domain.worker.entity.Worker;
import com.worksafe.backend.domain.worker.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IotServiceImplTest {

    @Mock
    private SensorLogRepository sensorLogRepository;
    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private RiskService riskService;
    @Mock
    private RiskEvaluationService riskEvaluationService;
    @Mock
    private RiskEventRepository riskEventRepository;
    @Mock
    private DroneDispatchRepository droneDispatchRepository;
    @Mock
    private DroneRepository droneRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private AlertRealtimeService alertRealtimeService;

    @InjectMocks
    private IotServiceImpl iotService;

    @Test
    void sosUsesServerMessageWithoutEquipment() {
        Worker worker = org.mockito.Mockito.mock(Worker.class);
        LocalDateTime measuredAt = LocalDateTime.of(2026, 10, 3, 10, 30);

        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(worker.getId()).thenReturn(1L);
        when(riskEventRepository.existsByWorker_IdAndRiskTypeAndStatusIn(any(), any(), any()))
                .thenReturn(false);
        when(sensorLogRepository.save(any(SensorLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        iotService.sos(new SosRequest(
                1L,
                37.5665,
                126.9780,
                measuredAt
        ));

        verify(equipmentRepository, never()).findById(any());
        verify(equipmentRepository, never()).findFirstByWorker_IdOrderByUpdatedAtDesc(any());

        ArgumentCaptor<SensorLog> sensorLogCaptor = ArgumentCaptor.forClass(SensorLog.class);
        verify(sensorLogRepository).save(sensorLogCaptor.capture());
        assertThat(sensorLogCaptor.getValue().getEquipment()).isNull();
        assertThat(sensorLogCaptor.getValue().getRawPayload()).isEqualTo("작업자 SOS 긴급 요청");

        ArgumentCaptor<RiskEventCreateRequest> riskRequestCaptor =
                ArgumentCaptor.forClass(RiskEventCreateRequest.class);
        verify(riskService).create(riskRequestCaptor.capture());
        assertThat(riskRequestCaptor.getValue().description()).isEqualTo("작업자 SOS 긴급 요청");
    }
}