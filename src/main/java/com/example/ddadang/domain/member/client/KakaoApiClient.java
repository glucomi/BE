package com.example.ddadang.domain.member.client;

import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 카카오 로그인 연동.
 * <ul>
 *   <li>앱: 카카오 SDK에서 받은 카카오 access token으로 사용자 정보를 조회해 토큰을 검증한다(앱 키 불필요).</li>
 *   <li>웹: 리다이렉트로 받은 인가 코드를 카카오 access token으로 교환한다(REST API 키 필요).</li>
 * </ul>
 */
@Slf4j
@Component
public class KakaoApiClient {

    private static final String INVALID_GRANT = "invalid_grant";

    private final RestClient kakaoRestClient;
    private final RestClient kakaoAuthRestClient;
    private final String clientId;
    private final String clientSecret;
    // Spring Boot 4 기본 Jackson 빈은 Jackson 3이라, 에러 바디 파싱용으로 직접 생성해 쓴다(IsensRestClientConfig와 동일).
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KakaoApiClient(
        @Value("${kakao.api-base-url}") String apiBaseUrl,
        @Value("${kakao.auth-base-url}") String authBaseUrl,
        @Value("${kakao.client-id:}") String clientId,
        @Value("${kakao.client-secret:}") String clientSecret
    ) {
        this.kakaoRestClient = RestClient.builder()
            .baseUrl(apiBaseUrl)
            .defaultStatusHandler(status -> status.is4xxClientError(), (request, response) -> {
                throw new GeneralException(AuthErrorStatus.INVALID_KAKAO_TOKEN);
            })
            .defaultStatusHandler(status -> status.is5xxServerError(), (request, response) -> {
                throw new GeneralException(AuthErrorStatus.KAKAO_SERVER_ERROR);
            })
            .build();
        this.kakaoAuthRestClient = RestClient.builder()
            .baseUrl(authBaseUrl)
            .defaultStatusHandler(status -> status.is4xxClientError(), (request, response) -> {
                throw tokenExchangeFailure(response);
            })
            .defaultStatusHandler(status -> status.is5xxServerError(), (request, response) -> {
                throw new GeneralException(AuthErrorStatus.KAKAO_SERVER_ERROR);
            })
            .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public KakaoUserResponse getUser(String kakaoAccessToken) {
        return kakaoRestClient.get()
            .uri("/v2/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
            .retrieve()
            .body(KakaoUserResponse.class);
    }

    /**
     * 인가 코드를 카카오 access token으로 교환한다. redirectUri는 인가 코드를 받을 때 쓴 값과 같아야 한다.
     */
    public KakaoTokenResponse exchangeCode(String code, String redirectUri) {
        if (clientId.isBlank()) {
            log.error("kakao.client-id(KAKAO_CLIENT_ID)가 설정되지 않아 인가 코드를 교환할 수 없습니다.");
            throw new GeneralException(AuthErrorStatus.KAKAO_LOGIN_NOT_CONFIGURED);
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        if (!clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }

        KakaoTokenResponse token = kakaoAuthRestClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(KakaoTokenResponse.class);
        if (token == null || token.accessToken() == null) {
            throw new GeneralException(AuthErrorStatus.KAKAO_SERVER_ERROR);
        }
        return token;
    }

    /**
     * invalid_grant(만료·재사용된 코드, redirect URI 불일치)는 클라이언트가 다시 로그인하면 되는 오류,
     * 그 외(REST API 키·Client Secret 오류 등)는 서버 설정 문제로 구분한다.
     */
    private GeneralException tokenExchangeFailure(ClientHttpResponse response) throws IOException {
        String error = "";
        String errorCode = "";
        try {
            JsonNode body = objectMapper.readTree(response.getBody());
            error = body.path("error").asText("");
            errorCode = body.path("error_code").asText("");
        } catch (IOException e) {
            // 바디를 읽지 못해도 상태 코드만으로 처리한다
        }
        if (INVALID_GRANT.equals(error)) {
            log.info("카카오 인가 코드 교환 거부: error={}, error_code={}", error, errorCode);
            return new GeneralException(AuthErrorStatus.INVALID_KAKAO_CODE);
        }
        log.error("카카오 인가 코드 교환 실패(설정 확인 필요): status={}, error={}, error_code={}",
            response.getStatusCode(), error, errorCode);
        return new GeneralException(AuthErrorStatus.KAKAO_LOGIN_NOT_CONFIGURED);
    }
}
