package com.worksafe.backend.global.security.jwt;

import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.global.security.exception.JwtTokenException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtKeyProvider {

    private final SecretKey signingKey;

    public JwtKeyProvider(JwtProperties jwtProperties) {
        try {
            byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            log.debug("JWT signing key initialized. length={}", keyBytes.length);
        } catch (WeakKeyException | IllegalArgumentException e) {
            throw new JwtTokenException(ErrorCode.JWT_INVALID_SECRET, e);
        }
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }
}
