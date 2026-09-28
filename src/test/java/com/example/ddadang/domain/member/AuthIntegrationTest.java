package com.example.ddadang.domain.member;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.client.KakaoUserResponse;
import com.example.ddadang.domain.member.client.KakaoUserResponse.KakaoAccount;
import com.example.ddadang.domain.member.client.KakaoUserResponse.Profile;
import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    void 카카오_첫_로그인시_회원가입되고_이후_로그인은_기존회원() throws Exception {
        given(kakaoApiClient.getUser(anyString()))
            .willReturn(new KakaoUserResponse(1001L, new KakaoAccount(new Profile("글루코미"))));

        mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kakaoAccessToken\":\"kakao-token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.isNewMember").value(true))
            .andExpect(jsonPath("$.result.signupCompleted").value(false))
            .andExpect(jsonPath("$.result.onboardingCompleted").value(false))
            .andExpect(jsonPath("$.result.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kakaoAccessToken\":\"kakao-token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.isNewMember").value(false));
    }

    @Test
    void 카카오_토큰이_유효하지_않으면_401() throws Exception {
        given(kakaoApiClient.getUser(anyString()))
            .willThrow(new GeneralException(AuthErrorStatus.INVALID_KAKAO_TOKEN));

        mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kakaoAccessToken\":\"bad\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_4010"));
    }

    @Test
    void refresh_token으로_재발급하면_이전_refresh_token은_사용불가() throws Exception {
        given(kakaoApiClient.getUser(anyString()))
            .willReturn(new KakaoUserResponse(1002L, new KakaoAccount(new Profile("회원"))));
        String loginBody = mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"kakaoAccessToken\":\"kakao-token\"}"))
            .andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(loginBody, "$.result.refreshToken");

        // 같은 초에 발급되면 토큰 문자열이 같아질 수 있어 발급 시각을 벌린다
        Thread.sleep(1100);
        mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_4013"));
    }

    @Test
    void 토큰_없이_보호된_API_호출시_401_JSON() throws Exception {
        mockMvc.perform(get("/api/members/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.code").value("COMMON_401"));
    }

    @Test
    void 잘못된_토큰으로_호출시_401_INVALID_TOKEN() throws Exception {
        mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer invalid.token.value"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_4011"));
    }
}
