package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DroneVideoCreateRequest(
        @NotBlank @Size(max = 500) String streamUrl,
        @NotNull VideoProtocol protocol,
        Long dispatchId,
        @Size(max = 200) String title,
        @Size(max = 500) String description
) {
}
