package com.worksafe.backend.domain.auth.service.impl;

import com.worksafe.backend.domain.auth.converter.AuthConverter;
import com.worksafe.backend.domain.auth.dto.request.ChangePasswordRequest;
import com.worksafe.backend.domain.auth.dto.request.LoginRequest;
import com.worksafe.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.worksafe.backend.domain.auth.dto.request.SignupRequest;
import com.worksafe.backend.domain.auth.dto.response.AuthTokenResponse;
import com.worksafe.backend.domain.auth.dto.response.AuthUserResponse;
import com.worksafe.backend.domain.auth.entity.RefreshToken;
import com.worksafe.backend.domain.auth.entity.User;
import com.worksafe.backend.domain.auth.enums.UserRole;
import com.worksafe.backend.domain.auth.repository.RefreshTokenRepository;
import com.worksafe.backend.domain.auth.repository.UserRepository;
import com.worksafe.backend.domain.auth.service.AuthService;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.global.security.jwt.JwtProperties;
import com.worksafe.backend.global.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public AuthUserResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.AUTH_DUPLICATED_LOGIN_ID);
        }

        User user = userRepository.save(User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role() == null ? UserRole.WORKER : request.role())
                .enabled(true)
                .build());

        return AuthConverter.toUserResponse(user);
    }

    @Override
    public AuthTokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.loginId(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, e);
        }

        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return issueTokens(user);
    }

    @Override
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        if (stored.isRevoked()) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
        }
        if (stored.isExpired()) {
            stored.revoke();
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }

        User user = stored.getUser();

        return issueTokens(user);
    }

    @Override
    public AuthUserResponse me(Long userId) {
        return AuthConverter.toUserResponse(findUser(userId));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    private AuthTokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.deleteByUser_Id(user.getId());
        refreshTokenRepository.flush();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpirationMs() * 1_000_000L))
                .revoked(false)
                .build());

        return AuthConverter.toTokenResponse(
                accessToken,
                refreshToken,
                user
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
