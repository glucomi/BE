package com.example.ddadang.domain.member.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST https://kauth.kakao.com/oauth/token 응답 중 사용하는 필드만 매핑한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenResponse(
    @JsonProperty("access_token") String accessToken
) {
}
