package com.worksafe.backend.domain.equipment.service;

import com.worksafe.backend.domain.equipment.dto.request.WearableCommandCreateRequest;
import com.worksafe.backend.domain.equipment.dto.response.WearableCommandResponse;

import java.util.List;

public interface WearableCommandService {

    WearableCommandResponse create(WearableCommandCreateRequest request);

    List<WearableCommandResponse> findPending();

    WearableCommandResponse acknowledge(Long commandId);
}
