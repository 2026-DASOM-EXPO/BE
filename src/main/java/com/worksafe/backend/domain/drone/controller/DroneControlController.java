package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.control.dto.DroneControlRequest;
import com.worksafe.backend.domain.drone.control.dto.DroneControlResponse;
import com.worksafe.backend.domain.drone.control.service.DroneControlService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "드론 제어 API", description = "Jetson에 전달할 기초 비행 조작 명령")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drones/{droneId}/commands")
public class DroneControlController {

    private final DroneControlService droneControlService;

    @PostMapping
    @Operation(summary = "드론 기초 조작 명령 전달")
    public ApiResponse<DroneControlResponse> send(
            @PathVariable Long droneId,
            @Valid @RequestBody DroneControlRequest request
    ) {
        return ApiResponse.success("명령 접수", droneControlService.send(droneId, request));
    }
}
