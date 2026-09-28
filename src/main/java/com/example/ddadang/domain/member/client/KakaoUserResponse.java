package com.example.ddadang.domain.member.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GET https://kapi.kakao.com/v2/user/me 응답 중 사용하는 필드만 매핑한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserResponse(
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(Profile profile) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(String nickname) {
    }
}
