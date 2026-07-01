package com.worksafe.backend.domain.auth.controller;

import com.worksafe.backend.domain.auth.dto.request.ChangePasswordRequest;
import com.worksafe.backend.domain.auth.dto.request.LoginRequest;
import com.worksafe.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.worksafe.backend.domain.auth.dto.request.SignupRequest;
import com.worksafe.backend.domain.auth.dto.response.AuthTokenResponse;
import com.worksafe.backend.domain.auth.dto.response.AuthUserResponse;
import com.worksafe.backend.domain.auth.service.AuthService;
import com.worksafe.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.created(authService.signup(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<AuthUserResponse> me(Authentication authentication) {
        return ApiResponse.success(authService.me(extractUserId(authentication)));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(extractUserId(authentication), request);
        return ApiResponse.success();
    }

    private Long extractUserId(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("Authenticated principal is missing user id");
    }
}
