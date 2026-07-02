package com.worksafe.backend.domain.drone.controller;

import com.worksafe.backend.domain.drone.dto.response.DroneVideoResponse;
import com.worksafe.backend.domain.drone.service.DroneService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "드론 영상 API", description = "드론 영상 시작 및 중지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drone-videos")
public class DroneVideoController {

    private final DroneService droneService;

    @PatchMapping("/{videoId}/start")
    @Operation(summary = "드론 영상 송출 시작")
    public ApiResponse<DroneVideoResponse> startVideo(@PathVariable Long videoId) {
        return ApiResponse.success(droneService.startVideo(videoId));
    }

    @PatchMapping("/{videoId}/stop")
    @Operation(summary = "드론 영상 송출 중지")
    public ApiResponse<DroneVideoResponse> stopVideo(@PathVariable Long videoId) {
        return ApiResponse.success(droneService.stopVideo(videoId));
    }
}
