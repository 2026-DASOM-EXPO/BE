package com.worksafe.backend.domain.drone.dto.request;

import com.worksafe.backend.domain.drone.enums.VideoProtocol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DroneVideoCreateRequest(
        @NotBlank @Size(max = 500) String streamUrl,
        @NotNull VideoProtocol protocol,
        Long dispatchId,
        @Size(max = 200) String title,
        @Size(max = 500) String description,
        @Min(320) @Max(3840) Integer width,
        @Min(240) @Max(2160) Integer height,
        @Min(1) @Max(60) Integer frameRate
) {
}
