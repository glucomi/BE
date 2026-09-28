package com.example.ddadang.domain.member.client;

import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 앱(카카오 SDK)에서 받은 카카오 access token으로 사용자 정보를 조회해 토큰을 검증한다.
 * 사용자 토큰으로 호출하는 API라 BE에는 카카오 앱 키가 필요 없다.
 */
@Component
public class KakaoApiClient {

    private final RestClient kakaoRestClient;

    public KakaoApiClient(@Value("${kakao.api-base-url}") String apiBaseUrl) {
        this.kakaoRestClient = RestClient.builder()
            .baseUrl(apiBaseUrl)
            .defaultStatusHandler(status -> status.is4xxClientError(), (request, response) -> {
                throw new GeneralException(AuthErrorStatus.INVALID_KAKAO_TOKEN);
            })
            .defaultStatusHandler(status -> status.is5xxServerError(), (request, response) -> {
                throw new GeneralException(AuthErrorStatus.KAKAO_SERVER_ERROR);
            })
            .build();
    }

    public KakaoUserResponse getUser(String kakaoAccessToken) {
        return kakaoRestClient.get()
            .uri("/v2/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
            .retrieve()
            .body(KakaoUserResponse.class);
    }
}
