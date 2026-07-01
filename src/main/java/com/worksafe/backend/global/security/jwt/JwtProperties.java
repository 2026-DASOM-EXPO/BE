package com.worksafe.backend.global.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@RequiredArgsConstructor
public class JwtProperties {

    @NotBlank
    private final String secret;
    @Positive
    private final long accessTokenExpirationMs;
    @Positive
    private final long refreshTokenExpirationMs;
}
