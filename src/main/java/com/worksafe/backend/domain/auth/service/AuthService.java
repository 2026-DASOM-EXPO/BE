package com.worksafe.backend.domain.auth.service;

import com.worksafe.backend.domain.auth.dto.request.ChangePasswordRequest;
import com.worksafe.backend.domain.auth.dto.request.LoginRequest;
import com.worksafe.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.worksafe.backend.domain.auth.dto.request.SignupRequest;
import com.worksafe.backend.domain.auth.dto.response.AuthTokenResponse;
import com.worksafe.backend.domain.auth.dto.response.AuthUserResponse;

public interface AuthService {

    AuthUserResponse signup(SignupRequest request);

    AuthTokenResponse login(LoginRequest request);

    AuthTokenResponse refresh(RefreshTokenRequest request);

    AuthUserResponse me(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);
}
