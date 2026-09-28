package com.example.ddadang.domain.glucose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.glucose.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.service.IsensCgmApiClient;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.support.IntegrationTest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CgmIntegrationTest extends IntegrationTest {

    private static final String START = "2026-09-01T00:00:00+09:00";
    private static final String END = "2026-09-02T00:00:00+09:00";

    @MockitoBean
    private IsensCgmApiClient isensCgmApiClient;

    @Autowired
    private CgmConnectionRepository cgmConnectionRepository;

    @Test
    void 인가_URL은_로그인_회원에게_state를_서명해_발급한다() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/api/cgm/oauth/authorize-url").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.authorizeUrl").value(org.hamcrest.Matchers.containsString("state=")));
    }

    @Test
    void 콜백_state가_유효하지_않으면_400() throws Exception {
        mockMvc.perform(get("/api/cgm/oauth/callback").param("code", "code").param("state", "forged"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CGM_4001"));
    }

    @Test
    void CGM_API는_인증이_필요하다() throws Exception {
        mockMvc.perform(get("/api/cgm/readings").param("start", START).param("end", END))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 연결된_CGM이_없으면_404() throws Exception {
        Member member = createMember();

        mockMvc.perform(post("/api/cgm/sync").param("start", START).param("end", END).header("Authorization", bearer(member)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CGM_4040"));
    }

    @Test
    void 동기화한_데이터는_회원_기준으로_저장되고_본인_데이터만_조회된다() throws Exception {
        Member member = createMember();
        Member other = createMember();
        CgmConnection connection = connect(member);
        connect(other);

        OffsetDateTime first = OffsetDateTime.parse("2026-09-01T09:00:00+09:00");
        given(isensCgmApiClient.fetchCgmData(eq("access-" + member.getId()), any(), any())).willReturn(List.of(
            sample("SN-1", 1L, first, 110.0),
            sample("SN-1", 2L, first.plusMinutes(5), 125.0)
        ));

        mockMvc.perform(post("/api/cgm/sync").param("start", START).param("end", END).header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.insertedCount").value(2));

        mockMvc.perform(get("/api/cgm/readings").param("start", START).param("end", END).header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(2))
            .andExpect(jsonPath("$.result.content[0].value").value(125.0));

        mockMvc.perform(get("/api/cgm/readings").param("start", START).param("end", END).header("Authorization", bearer(other)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(0));

        CgmConnection updated = cgmConnectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(updated.getSensorSerial()).isEqualTo("SN-1");
        assertThat(updated.getSensorStartedAt()).isEqualTo(first);
    }

    private CgmConnection connect(Member member) {
        return cgmConnectionRepository.save(CgmConnection.connect(
            member, CgmProvider.CARESENS_AIR, UUID.randomUUID().toString(),
            "access-" + member.getId(), "refresh", LocalDateTime.now().plusHours(1)
        ));
    }

    private CgmSampleResponse sample(String serial, Long seq, OffsetDateTime eventAt, Double value) {
        return new CgmSampleResponse(serial, seq, eventAt, 540, 2, value, value, 0.0, 4, 0, 0);
    }
}
