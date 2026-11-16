package com.example.ddadang.domain.record.glucose;

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

class GlucoseRecordIntegrationTest extends IntegrationTest {

    @Test
    void 혈당_기록을_등록하고_날짜별로_시간순_조회한다() throws Exception {
        Member me = createMember();
        create(me, "2026-10-05T12:00:00", 160, "점심 후");
        create(me, "2026-10-05T08:00:00", 105, null);
        create(me, "2026-10-06T00:00:00", 98, null);
        create(createMember(), "2026-10-05T09:00:00", 120, null);

        mockMvc.perform(get("/api/glucose-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].measuredAt").value("2026-10-05T08:00:00"))
            .andExpect(jsonPath("$.result[0].valueMgDl").value(105))
            .andExpect(jsonPath("$.result[0].memo").doesNotExist())
            .andExpect(jsonPath("$.result[1].memo").value("점심 후"));
    }

    @Test
    void 혈당_기록을_수정하고_삭제한다() throws Exception {
        Member me = createMember();
        Integer id = create(me, "2026-10-05T08:00:00", 105, "공복");

        mockMvc.perform(put("/api/glucose-records/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"2026-10-05T07:30:00\",\"valueMgDl\":99,\"memo\":\"\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.measuredAt").value("2026-10-05T07:30:00"))
            .andExpect(jsonPath("$.result.valueMgDl").value(99))
            .andExpect(jsonPath("$.result.memo").doesNotExist());

        mockMvc.perform(delete("/api/glucose-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/glucose-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("GLUCOSE_RECORD_4040"));
    }

    @Test
    void 다른_회원의_혈당_기록은_조회_수정_삭제할_수_없다() throws Exception {
        Integer id = create(createMember(), "2026-10-05T08:00:00", 105, null);
        String other = bearer(createMember());

        mockMvc.perform(get("/api/glucose-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/glucose-records/{id}", id)
                .header("Authorization", other)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"2026-10-05T08:00:00\",\"valueMgDl\":100}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/glucose-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
    }

    @Test
    void 혈당값이_범위를_벗어나면_400() throws Exception {
        Member me = createMember();

        mockMvc.perform(post("/api/glucose-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"2026-10-05T08:00:00\",\"valueMgDl\":601}"))
            .andExpect(status().isBadRequest());
    }

    private Integer create(Member member, String measuredAt, int value, String memo) throws Exception {
        String memoJson = memo == null ? "null" : "\"" + memo + "\"";
        String body = mockMvc.perform(post("/api/glucose-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measuredAt\":\"%s\",\"valueMgDl\":%d,\"memo\":%s}".formatted(measuredAt, value, memoJson)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.glucoseRecordId");
    }
}
