package com.example.ddadang.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.support.IntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OnboardingIntegrationTest extends IntegrationTest {

    private static final String ONBOARDING_BODY = """
        {"diabetesType":"TYPE2_INSULIN","heightCm":168,"weightKg":47.7,"targetGlucoseMin":70,"targetGlucoseMax":180}
        """;

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    @Test
    void 온보딩_저장시_회원정보와_첫_체중기록이_저장된다() throws Exception {
        Member member = signedUpMember();

        mockMvc.perform(put("/api/members/me/onboarding")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ONBOARDING_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.onboardingCompleted").value(true))
            .andExpect(jsonPath("$.result.diabetesType").value("TYPE2_INSULIN"))
            .andExpect(jsonPath("$.result.targetGlucoseMax").value(180));

        List<WeightRecord> weights = weightRecordRepository.findByMemberIdOrderByMeasuredAtDesc(member.getId());
        assertThat(weights).hasSize(1);
        assertThat(weights.get(0).getWeightKg()).isEqualByComparingTo(new BigDecimal("47.7"));

        mockMvc.perform(get("/api/members/me").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.onboardingCompleted").value(true))
            .andExpect(jsonPath("$.result.heightCm").value(168.0));
    }

    @Test
    void 약관동의_전이면_403() throws Exception {
        Member member = createMember();

        mockMvc.perform(put("/api/members/me/onboarding")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ONBOARDING_BODY))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("MEMBER_4030"));
    }

    @Test
    void 목표혈당_하한이_상한보다_크거나_같으면_400() throws Exception {
        Member member = signedUpMember();

        mockMvc.perform(put("/api/members/me/onboarding")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"diabetesType":"TYPE1","heightCm":170,"weightKg":60,"targetGlucoseMin":180,"targetGlucoseMax":180}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MEMBER_4002"));
    }

    @Test
    void 입력값_범위를_벗어나면_400() throws Exception {
        Member member = signedUpMember();

        mockMvc.perform(put("/api/members/me/onboarding")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"diabetesType":"TYPE1","heightCm":10,"weightKg":60,"targetGlucoseMin":70,"targetGlucoseMax":180}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    private Member signedUpMember() {
        Member member = createMember();
        member.completeSignup(false);
        return memberRepository.save(member);
    }
}
