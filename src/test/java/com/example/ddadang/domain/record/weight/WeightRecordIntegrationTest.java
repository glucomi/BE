package com.example.ddadang.domain.record.weight;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class WeightRecordIntegrationTest extends IntegrationTest {

    @Test
    void 체중_기록을_등록하고_날짜별로_시간순_조회한다() throws Exception {
        Member me = createMember();
        create(me, "2026-10-05T21:00:00", "48.1");
        create(me, "2026-10-05T07:00:00", "47.7");
        create(me, "2026-10-04T23:59:59", "47.9");
        create(createMember(), "2026-10-05T08:00:00", "70.0");

        mockMvc.perform(get("/api/weight-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].measuredAt").value("2026-10-05T07:00:00"))
            .andExpect(jsonPath("$.result[0].weightKg").value(47.7))
            .andExpect(jsonPath("$.result[1].weightKg").value(48.1));
    }

    @Test
    void 체중_기록을_수정하고_삭제한다() throws Exception {
        Member me = createMember();
        Integer id = create(me, "2026-10-05T07:00:00", "47.7");

        mockMvc.perform(put("/api/weight-records/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"2026-10-05T08:00:00\",\"weightKg\":47.2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.measuredAt").value("2026-10-05T08:00:00"))
            .andExpect(jsonPath("$.result.weightKg").value(47.2));

        mockMvc.perform(delete("/api/weight-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/weight-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("WEIGHT_RECORD_4040"));
    }

    @Test
    void 다른_회원의_체중_기록은_조회_수정_삭제할_수_없다() throws Exception {
        Integer id = create(createMember(), "2026-10-05T07:00:00", "47.7");
        String other = bearer(createMember());

        mockMvc.perform(get("/api/weight-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/weight-records/{id}", id)
                .header("Authorization", other)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"2026-10-05T07:00:00\",\"weightKg\":50}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/weight-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
    }

    @Test
    void 체중이_범위를_벗어나거나_소수_2자리면_400() throws Exception {
        Member me = createMember();

        for (String weight : new String[] {"19.9", "300.1", "47.75"}) {
            mockMvc.perform(post("/api/weight-records")
                    .header("Authorization", bearer(me))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"measuredAt\":\"2026-10-05T07:00:00\",\"weightKg\":%s}".formatted(weight)))
                .andExpect(status().isBadRequest());
        }
    }

    private Integer create(Member member, String measuredAt, String weightKg) throws Exception {
        String body = mockMvc.perform(post("/api/weight-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"%s\",\"weightKg\":%s}".formatted(measuredAt, weightKg)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.weightRecordId");
    }
}
