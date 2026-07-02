package com.worksafe.backend.domain.iot.controller;

import com.worksafe.backend.domain.iot.dto.request.AttendanceRequest;
import com.worksafe.backend.domain.iot.dto.request.BiometricRequest;
import com.worksafe.backend.domain.iot.dto.request.DroneObstacleRequest;
import com.worksafe.backend.domain.iot.dto.request.EquipmentStatusRequest;
import com.worksafe.backend.domain.iot.dto.request.GpsRequest;
import com.worksafe.backend.domain.iot.dto.request.ImuRequest;
import com.worksafe.backend.domain.iot.dto.request.SosRequest;
import com.worksafe.backend.domain.iot.dto.response.AttendanceResponse;
import com.worksafe.backend.domain.iot.service.IotService;
import com.worksafe.backend.domain.risk.dto.response.RiskEventResponse;
import com.worksafe.backend.domain.sensor.dto.response.SensorLogResponse;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "IoT 수집 API", description = "RFID 출입 인증, 생체 데이터, IMU, GPS, 안전장비, SOS, 드론 장애물 센서 수집")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/iot")
public class IotController {

    private final IotService iotService;

    @PostMapping("/attendance")
    @Operation(summary = "RFID 출입 인증")
    public ApiResponse<AttendanceResponse> attendance(@Valid @RequestBody AttendanceRequest request) {
        return ApiResponse.created(iotService.attendance(request));
    }

    @PostMapping("/biometrics")
    @Operation(summary = "생체 데이터 수집")
    public ApiResponse<SensorLogResponse> biometrics(@Valid @RequestBody BiometricRequest request) {
        return ApiResponse.created(iotService.biometrics(request));
    }

    @PostMapping("/imu")
    @Operation(summary = "IMU 데이터 수집")
    public ApiResponse<SensorLogResponse> imu(@Valid @RequestBody ImuRequest request) {
        return ApiResponse.created(iotService.imu(request));
    }

    @PostMapping("/gps")
    @Operation(summary = "GPS 위치 수집")
    public ApiResponse<SensorLogResponse> gps(@Valid @RequestBody GpsRequest request) {
        return ApiResponse.created(iotService.gps(request));
    }

    @PostMapping("/equipment-status")
    @Operation(summary = "안전장비 착용 상태 변경")
    public ApiResponse<SensorLogResponse> equipmentStatus(@Valid @RequestBody EquipmentStatusRequest request) {
        return ApiResponse.created(iotService.equipmentStatus(request));
    }

    @PostMapping("/sos")
    @Operation(summary = "SOS 긴급 신고")
    public ApiResponse<RiskEventResponse> sos(@Valid @RequestBody SosRequest request) {
        return ApiResponse.created(iotService.sos(request));
    }

    @PostMapping("/drone-obstacle")
    @Operation(summary = "드론 장애물 센서 수집")
    public ApiResponse<SensorLogResponse> droneObstacle(@Valid @RequestBody DroneObstacleRequest request) {
        return ApiResponse.created(iotService.droneObstacle(request));
    }
}
