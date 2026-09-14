package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.config.IsensProperties;
import com.example.ddadang.domain.cgm.dto.response.IsensTokenResponse;
import com.example.ddadang.domain.cgm.entity.CgmToken;
import com.example.ddadang.domain.cgm.repository.CgmTokenRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class IsensAuthService {

    private static final String SCOPE_OFFLINE_ACCESS = "offline_access";

    private final IsensProperties isensProperties;
    private final CgmTokenRepository cgmTokenRepository;

    @Qualifier("isensAuthRestClient")
    private final RestClient isensAuthRestClient;

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder
            .fromUriString(isensProperties.authBaseUrl())
            .path("/auth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", isensProperties.clientId())
            .queryParam("redirect_uri", isensProperties.redirectUri())
            .queryParam("state", state)
            .queryParam("scope", SCOPE_OFFLINE_ACCESS)
            .build()
            .toUriString();
    }

    /**
     * 인가 코드를 access token / refresh token으로 교환하고 CgmToken을 upsert한다.
     */
    public CgmToken exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", isensProperties.clientId());
        form.add("client_secret", isensProperties.clientSecret());
        form.add("redirect_uri", isensProperties.redirectUri());

        IsensTokenResponse tokenResponse = requestToken(form);
        return saveOrUpdateToken(tokenResponse);
    }

    /**
     * TODO: refresh 요청/응답 스펙이 개발가이드에서 미확인 상태(doc021). 표준 OAuth2
     * refresh_token 그랜트 형태로 구현해두었으나, 실제 파라미터명/응답 포맷은 문서 확인 후 검증 필요.
     */
    public CgmToken refreshAccessToken(CgmToken existingToken) {
        if (existingToken.getRefreshToken() == null) {
            throw new IllegalStateException("refresh_token이 없어 갱신할 수 없습니다. 재인증이 필요합니다.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", existingToken.getRefreshToken());
        form.add("client_id", isensProperties.clientId());
        form.add("client_secret", isensProperties.clientSecret());

        IsensTokenResponse tokenResponse = requestToken(form);
        existingToken.updateTokens(
            tokenResponse.accessToken(),
            tokenResponse.refreshToken(),
            tokenResponse.tokenType(),
            LocalDateTime.now().plusSeconds(tokenResponse.expiresIn())
        );
        return existingToken;
    }

    /**
     * 저장된 토큰이 만료됐으면 refresh 후, 유효한 access token 문자열을 반환한다.
     */
    public String getValidAccessToken(String isensUserId) {
        CgmToken token = cgmTokenRepository.findByIsensUserId(isensUserId)
            .orElseThrow(() -> new IllegalStateException("연동된 i-sens 계정이 없습니다: " + isensUserId));

        if (token.isExpired()) {
            token = refreshAccessToken(token);
        }
        return token.getAccessToken();
    }

    private IsensTokenResponse requestToken(MultiValueMap<String, String> form) {
        return isensAuthRestClient.post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(IsensTokenResponse.class);
    }

    private CgmToken saveOrUpdateToken(IsensTokenResponse tokenResponse) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.expiresIn());

        return cgmTokenRepository.findByIsensUserId(tokenResponse.userId())
            .map(existing -> {
                existing.updateTokens(
                    tokenResponse.accessToken(),
                    tokenResponse.refreshToken(),
                    tokenResponse.tokenType(),
                    expiresAt
                );
                return existing;
            })
            .orElseGet(() -> cgmTokenRepository.save(new CgmToken(
                tokenResponse.userId(),
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                tokenResponse.tokenType(),
                expiresAt
            )));
    }
}
