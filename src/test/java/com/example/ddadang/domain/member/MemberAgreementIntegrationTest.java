package com.example.ddadang.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MemberAgreementIntegrationTest extends IntegrationTest {

    @Test
    void 약관_목록은_인증없이_조회된다() throws Exception {
        mockMvc.perform(get("/api/terms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(5))
            .andExpect(jsonPath("$.result[0].termsType").value("AGE_OVER_14"))
            .andExpect(jsonPath("$.result[0].required").value(true));
    }

    @Test
    void 필수약관_모두_동의하면_회원가입_완료되고_마케팅동의_반영() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/api/members/me/agreements")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"agreements":[
                      {"termsType":"AGE_OVER_14","agreed":true},
                      {"termsType":"SERVICE","agreed":true},
                      {"termsType":"PRIVACY","agreed":true},
                      {"termsType":"SERVICE_IMPROVEMENT","agreed":false},
                      {"termsType":"MARKETING","agreed":true}
                    ]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.signupCompleted").value(true))
            .andExpect(jsonPath("$.result.onboardingCompleted").value(false));

        Member saved = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(saved.isSignupCompleted()).isTrue();
        assertThat(saved.isMarketingAgreed()).isTrue();
    }

    @Test
    void 필수약관_미동의시_400() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/api/members/me/agreements")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"agreements":[
                      {"termsType":"AGE_OVER_14","agreed":true},
                      {"termsType":"SERVICE","agreed":true},
                      {"termsType":"PRIVACY","agreed":false}
                    ]}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MEMBER_4000"));
    }

    @Test
    void 존재하지_않는_약관_타입이면_400() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/api/members/me/agreements")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"agreements\":[{\"termsType\":\"UNKNOWN\",\"agreed\":true}]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }
}
