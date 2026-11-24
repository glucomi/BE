package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.dto.response.KakaoAuthorizeUrlResponse;
import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 웹 카카오 로그인 인가 URL을 만든다. 프론트가 카카오 REST API 키를 갖지 않도록 서버에서 조립한다.
 * redirectUri는 허용 목록(kakao.allowed-redirect-uris)에 있는 값만 받는다.
 */
@Slf4j
@Service
public class KakaoAuthorizeUrlService {

    private static final Pattern STATE_PATTERN = Pattern.compile("[A-Za-z0-9._~-]{1,128}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String authBaseUrl;
    private final String clientId;
    private final List<String> allowedRedirectUris;

    public KakaoAuthorizeUrlService(
        @Value("${kakao.auth-base-url}") String authBaseUrl,
        @Value("${kakao.client-id:}") String clientId,
        @Value("${kakao.allowed-redirect-uris:}") List<String> allowedRedirectUris
    ) {
        this.authBaseUrl = authBaseUrl;
        this.clientId = clientId;
        this.allowedRedirectUris = allowedRedirectUris.stream().map(String::trim).filter(uri -> !uri.isEmpty()).toList();
    }

    /**
     * @param state 프론트가 만든 state. 비어 있으면 서버가 만든다.
     */
    public KakaoAuthorizeUrlResponse buildAuthorizeUrl(String redirectUri, String state) {
        if (!allowedRedirectUris.contains(redirectUri)) {
            throw new GeneralException(AuthErrorStatus.INVALID_REDIRECT_URI);
        }
        String resolvedState = (state == null || state.isEmpty()) ? generateState() : state;
        if (!STATE_PATTERN.matcher(resolvedState).matches()) {
            throw new GeneralException(AuthErrorStatus.INVALID_STATE);
        }
        if (clientId.isBlank()) {
            log.error("kakao.client-id(KAKAO_CLIENT_ID)가 설정되지 않아 카카오 인가 URL을 만들 수 없습니다.");
            throw new GeneralException(AuthErrorStatus.KAKAO_LOGIN_NOT_CONFIGURED);
        }

        String authorizeUrl = UriComponentsBuilder.fromUriString(authBaseUrl)
            .path("/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", encode(clientId))
            .queryParam("redirect_uri", encode(redirectUri))
            .queryParam("state", resolvedState)
            .build(true)
            .toUriString();
        return new KakaoAuthorizeUrlResponse(authorizeUrl, resolvedState);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String generateState() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
