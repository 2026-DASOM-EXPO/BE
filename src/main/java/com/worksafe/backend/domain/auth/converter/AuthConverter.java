package com.worksafe.backend.domain.auth.converter;

import com.worksafe.backend.domain.auth.dto.response.AuthTokenResponse;
import com.worksafe.backend.domain.auth.dto.response.AuthUserResponse;
import com.worksafe.backend.domain.auth.entity.User;

public final class AuthConverter {

    private AuthConverter() {
    }

    public static AuthUserResponse toUserResponse(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getRole()
        );
    }

    public static AuthTokenResponse toTokenResponse(
            String accessToken,
            String refreshToken,
            User user
    ) {
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                toUserResponse(user)
        );
    }
}
