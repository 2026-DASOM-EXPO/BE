package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.streaming.DroneStreamGateway;
import com.worksafe.backend.domain.drone.streaming.DroneStreamStatus;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "드론 스트리밍 API", description = "FFmpeg 기반 드론 RTSP 입력과 HLS 송출 상태 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drone-streams")
public class DroneStreamController {

    private final DroneStreamGateway droneStreamGateway;

    @PostMapping("/{streamKey}/start")
    @Operation(summary = "드론 RTSP → HLS 송출 시작")
    public ApiResponse<DroneStreamStatus> start(@PathVariable String streamKey) {
        return ApiResponse.success(droneStreamGateway.start(streamKey));
    }

    @GetMapping("/{streamKey}/status")
    @Operation(summary = "드론 HLS 송출 상태 조회")
    public ApiResponse<DroneStreamStatus> status(@PathVariable String streamKey) {
        return ApiResponse.success(droneStreamGateway.status(streamKey));
    }

    @DeleteMapping("/{streamKey}")
    @Operation(summary = "드론 HLS 송출 중지")
    public ApiResponse<DroneStreamStatus> stop(@PathVariable String streamKey) {
        return ApiResponse.success(droneStreamGateway.stop(streamKey));
    }
}
