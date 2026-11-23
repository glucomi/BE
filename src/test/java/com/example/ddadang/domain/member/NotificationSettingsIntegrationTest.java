package com.example.ddadang.domain.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class NotificationSettingsIntegrationTest extends IntegrationTest {

    @Test
    void 가입_직후에는_혈당알림_켜짐_마케팅_미동의() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/api/members/me/notification-settings").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.pushGlucoseEnabled").value(true))
            .andExpect(jsonPath("$.result.marketingAgreed").value(false));
    }

    @Test
    void 약관동의에서_마케팅에_동의하면_true로_조회된다() throws Exception {
        Member member = createMember();
        member.completeSignup(true);
        memberRepository.save(member);

        mockMvc.perform(get("/api/members/me/notification-settings").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.marketingAgreed").value(true));
    }

    @Test
    void 토큰_없이_조회하면_401() throws Exception {
        mockMvc.perform(get("/api/members/me/notification-settings"))
            .andExpect(status().isUnauthorized());
    }
}
