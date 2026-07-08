package com.worksafe.backend.domain.auth.dto.response;

import com.worksafe.backend.domain.auth.enums.UserRole;

public record AuthUserResponse(
        Long id,
        String loginId,
        String name,
        UserRole role
) {
}
