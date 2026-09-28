package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.config.IsensProperties;
import com.example.ddadang.domain.glucose.dto.response.IsensTokenResponse;
import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.enums.CgmConnectionStatus;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.exception.InvalidOAuthStateException;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.status.CgmErrorStatus;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.global.exception.GeneralException;
import com.example.ddadang.global.security.jwt.JwtProvider;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class IsensAuthService {

    private static final String SCOPE_OFFLINE_ACCESS = "offline_access";
    private static final String OAUTH_STATE_PURPOSE = "cgm_oauth_state";
    private static final Duration OAUTH_STATE_VALIDITY = Duration.ofMinutes(10);

    private final IsensProperties isensProperties;
    private final CgmConnectionRepository cgmConnectionRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @Qualifier("isensAuthRestClient")
    private final RestClient isensAuthRestClient;

    /**
     * 콜백은 브라우저 리다이렉트라 Authorization 헤더가 없으므로, 로그인 회원 ID를 서명한 단기 토큰을
     * state로 넘겨 콜백에서 회원을 식별한다(서명 검증으로 CSRF도 함께 방어).
     */
    public String buildAuthorizeUrl(Long memberId) {
        String state = jwtProvider.createPurposeToken(memberId, OAUTH_STATE_PURPOSE, OAUTH_STATE_VALIDITY);
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

    public Long resolveMemberIdFromState(String state) {
        try {
            return jwtProvider.parsePurposeToken(state, OAUTH_STATE_PURPOSE);
        } catch (GeneralException e) {
            throw new InvalidOAuthStateException();
        }
    }

    /**
     * 인가 코드를 토큰으로 교환하고 회원의 케어센스 연결을 생성/갱신한다.
     */
    @Transactional
    public CgmConnection connect(Long memberId, String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", isensProperties.clientId());
        form.add("client_secret", isensProperties.clientSecret());
        form.add("redirect_uri", isensProperties.redirectUri());

        IsensTokenResponse token = requestToken(form);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(token.expiresIn());

        return cgmConnectionRepository.findByProviderAndExternalUserId(CgmProvider.CARESENS_AIR, token.userId())
            .map(existing -> {
                if (!existing.isOwnedBy(memberId)) {
                    throw new GeneralException(CgmErrorStatus.CGM_ALREADY_LINKED);
                }
                existing.reconnect(token.accessToken(), token.refreshToken(), expiresAt);
                return existing;
            })
            .orElseGet(() -> cgmConnectionRepository.save(CgmConnection.connect(
                memberRepository.getReferenceById(memberId), CgmProvider.CARESENS_AIR, token.userId(),
                token.accessToken(), token.refreshToken(), expiresAt
            )));
    }

    @Transactional(readOnly = true)
    public CgmConnection getConnection(Long memberId) {
        return cgmConnectionRepository.findFirstByMemberIdAndProviderAndStatusOrderByUpdatedAtDesc(
                memberId, CgmProvider.CARESENS_AIR, CgmConnectionStatus.CONNECTED)
            .orElseThrow(() -> new GeneralException(CgmErrorStatus.CGM_NOT_CONNECTED));
    }

    /**
     * 저장된 토큰이 만료됐으면 refresh 후, 유효한 access token 문자열을 반환한다.
     */
    @Transactional
    public String getValidAccessToken(Long memberId) {
        CgmConnection connection = getConnection(memberId);
        if (connection.isTokenExpired()) {
            refreshAccessToken(connection);
        }
        return connection.getAccessToken();
    }

    /**
     * TODO: refresh 요청/응답 스펙이 개발가이드에서 미확인 상태(doc021). 표준 OAuth2
     * refresh_token 그랜트 형태로 구현해두었으나, 실제 파라미터명/응답 포맷은 문서 확인 후 검증 필요.
     */
    private void refreshAccessToken(CgmConnection connection) {
        if (connection.getRefreshToken() == null) {
            throw new GeneralException(CgmErrorStatus.CGM_REAUTH_REQUIRED);
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", connection.getRefreshToken());
        form.add("client_id", isensProperties.clientId());
        form.add("client_secret", isensProperties.clientSecret());

        IsensTokenResponse token = requestToken(form);
        connection.updateTokens(
            token.accessToken(), token.refreshToken(), LocalDateTime.now().plusSeconds(token.expiresIn())
        );
    }

    private IsensTokenResponse requestToken(MultiValueMap<String, String> form) {
        return isensAuthRestClient.post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(IsensTokenResponse.class);
    }
}
