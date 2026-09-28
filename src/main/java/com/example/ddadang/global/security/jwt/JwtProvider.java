package com.example.ddadang.global.security.jwt;

import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtProvider {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, ACCESS, jwtProperties.accessTokenValidity());
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, REFRESH, jwtProperties.refreshTokenValidity());
    }

    public LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plus(jwtProperties.refreshTokenValidity());
    }

    public Long parseAccessToken(String token) {
        return parse(token, ACCESS);
    }

    public Long parseRefreshToken(String token) {
        return parse(token, REFRESH);
    }

    private String createToken(Long memberId, String type, Duration validity) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim(TOKEN_TYPE_CLAIM, type)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(validity)))
            .signWith(secretKey)
            .compact();
    }

    private Long parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
            }
            return Long.valueOf(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new GeneralException(AuthErrorStatus.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new GeneralException(AuthErrorStatus.INVALID_TOKEN);
        }
    }
}
