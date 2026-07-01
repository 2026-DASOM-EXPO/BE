package com.worksafe.backend.domain.equipment.controller;

import com.worksafe.backend.domain.equipment.dto.request.WearableCommandCreateRequest;
import com.worksafe.backend.domain.equipment.dto.response.WearableCommandResponse;
import com.worksafe.backend.domain.equipment.service.WearableCommandService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Wearable Commands")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wearable-commands")
public class WearableCommandController {

    private final WearableCommandService wearableCommandService;

    @PostMapping
    @Operation(summary = "웨어러블 명령 생성")
    public ApiResponse<WearableCommandResponse> create(@Valid @RequestBody WearableCommandCreateRequest request) {
        return ApiResponse.created(wearableCommandService.create(request));
    }

    @GetMapping("/pending")
    @Operation(summary = "대기 중인 웨어러블 명령 조회")
    public ApiResponse<List<WearableCommandResponse>> findPending() {
        return ApiResponse.success(wearableCommandService.findPending());
    }

    @PatchMapping("/{commandId}/ack")
    @Operation(summary = "웨어러블 명령 ACK")
    public ApiResponse<WearableCommandResponse> acknowledge(@PathVariable Long commandId) {
        return ApiResponse.success(wearableCommandService.acknowledge(commandId));
    }
}
