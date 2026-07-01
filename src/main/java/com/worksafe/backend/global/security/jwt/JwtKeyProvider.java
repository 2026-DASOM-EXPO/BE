package com.worksafe.backend.global.security.jwt;

import com.worksafe.backend.global.common.exception.ErrorCode;
import com.worksafe.backend.global.security.exception.JwtTokenException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.io.DecodingException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtKeyProvider {

    private final SecretKey signingKey;

    public JwtKeyProvider(JwtProperties jwtProperties) {
        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());

            System.out.println("secret length = " + jwtProperties.getSecret().length());
            System.out.println("decoded length = " + keyBytes.length);

            this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        } catch (Exception e) {
            e.printStackTrace();   // 추가
            throw new JwtTokenException(ErrorCode.JWT_INVALID_SECRET);
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    "JWT 인증키가 생성되었습니다. (algorithm=HS256, keyLength={} bytes)",
                    keyBytes.length
            );
        }
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }
}
