package com.example.ddadang.domain.record.weight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class WeightRecordIntegrationTest extends IntegrationTest {

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    @Test
    void 같은_날짜에_다시_저장하면_새로_만들지_않고_갱신한다() throws Exception {
        Member me = createMember();
        save(me, "2026-10-05", "47.7");
        save(me, "2026-10-05", "48.1");

        mockMvc.perform(get("/api/weight-records/{date}", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.date").value("2026-10-05"))
            .andExpect(jsonPath("$.result.weightKg").value(48.1));
        assertThat(weightRecordRepository.findByMemberIdOrderByMeasuredAtDesc(me.getId())).hasSize(1);
    }

    @Test
    void 기록이_없는_날짜는_null이고_최근_체중은_가장_최근_날짜_기록이다() throws Exception {
        Member me = createMember();
        mockMvc.perform(get("/api/weight-records/latest").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").doesNotExist());

        save(me, "2026-10-03", "47.0");
        save(me, "2026-10-05", "47.7");
        save(me, "2026-10-04", "47.3");
        save(createMember(), "2026-10-06", "70.0");

        mockMvc.perform(get("/api/weight-records/{date}", "2026-10-06").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").doesNotExist());
        mockMvc.perform(get("/api/weight-records/latest").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.date").value("2026-10-05"))
            .andExpect(jsonPath("$.result.weightKg").value(47.7));
    }

    @Test
    void 날짜별_기록을_삭제한다() throws Exception {
        Member me = createMember();
        save(me, "2026-10-05", "47.7");

        mockMvc.perform(delete("/api/weight-records/{date}", "2026-10-05").header("Authorization", bearer(createMember())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("WEIGHT_RECORD_4040"));
        mockMvc.perform(delete("/api/weight-records/{date}", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/weight-records/{date}", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result").doesNotExist());
        mockMvc.perform(delete("/api/weight-records/{date}", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isNotFound());
    }

    @Test
    void 체중이_0_이하거나_200_초과거나_소수_2자리면_400() throws Exception {
        Member me = createMember();

        for (String weight : new String[] {"0", "200.1", "47.75"}) {
            mockMvc.perform(put("/api/weight-records/{date}", "2026-10-05")
                    .header("Authorization", bearer(me))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"weightKg\":%s}".formatted(weight)))
                .andExpect(status().isBadRequest());
        }
        save(me, "2026-10-05", "200.0");
    }

    private void save(Member member, String date, String weightKg) throws Exception {
        mockMvc.perform(put("/api/weight-records/{date}", date)
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"weightKg\":%s}".formatted(weightKg)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.date").value(date));
    }
}
