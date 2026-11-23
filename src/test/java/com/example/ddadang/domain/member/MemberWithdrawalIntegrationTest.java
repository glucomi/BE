package com.example.ddadang.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.client.KakaoUserResponse;
import com.example.ddadang.domain.member.client.KakaoUserResponse.KakaoAccount;
import com.example.ddadang.domain.member.client.KakaoUserResponse.Profile;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.MemberStatus;
import com.example.ddadang.domain.member.enums.WithdrawalReason;
import com.example.ddadang.domain.member.repository.MemberWithdrawalRepository;
import com.example.ddadang.domain.member.repository.RefreshTokenRepository;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class MemberWithdrawalIntegrationTest extends IntegrationTest {

    @Autowired
    private MemberWithdrawalRepository memberWithdrawalRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 탈퇴하면_탈퇴상태와_사유가_저장되고_재발급과_재로그인이_막힌다() throws Exception {
        given(kakaoApiClient.getUser(anyString()))
            .willReturn(new KakaoUserResponse(2001L, new KakaoAccount(new Profile("탈퇴회원"))));
        String login = mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"kakao-token\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Integer memberId = JsonPath.read(login, "$.result.memberId");
        String accessToken = JsonPath.read(login, "$.result.accessToken");
        String refreshToken = JsonPath.read(login, "$.result.refreshToken");

        mockMvc.perform(post("/api/members/me/withdrawal")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"DEVICE_INCONVENIENT\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true));

        Member member = memberRepository.findById(memberId.longValue()).orElseThrow();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(member.getDeletedAt()).isNotNull();
        assertThat(memberWithdrawalRepository.findByMemberId(member.getId()))
            .get().extracting("reason").isEqualTo(WithdrawalReason.DEVICE_INCONVENIENT);
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isEmpty();

        mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_4013"));

        mockMvc.perform(post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"kakao-token\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH_4030"));
    }

    @Test
    void 이미_탈퇴한_회원이_다시_탈퇴하면_403() throws Exception {
        Member member = createMember();
        String token = bearer(member);
        withdraw(token).andExpect(status().isOk());

        withdraw(token)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("AUTH_4030"));
    }

    @Test
    void 탈퇴사유가_없거나_잘못되면_400() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/api/members/me/withdrawal")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));

        mockMvc.perform(post("/api/members/me/withdrawal")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"UNKNOWN\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    @Test
    void 토큰_없이_탈퇴하면_401() throws Exception {
        mockMvc.perform(post("/api/members/me/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"RARELY_USED\"}"))
            .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions withdraw(String token) throws Exception {
        return mockMvc.perform(post("/api/members/me/withdrawal")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\":\"RARELY_USED\"}"));
    }
}
