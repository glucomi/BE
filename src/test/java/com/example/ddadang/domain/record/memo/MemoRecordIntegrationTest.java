package com.example.ddadang.domain.record.memo;

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

class MemoRecordIntegrationTest extends IntegrationTest {

    @Test
    void 메모_기록을_등록하고_날짜별로_시간순_조회한다() throws Exception {
        Member me = createMember();
        create(me, "2026-10-05T21:00:00", "  저녁 늦게 먹음  ");
        create(me, "2026-10-05T09:00:00", "산책 30분");
        create(me, "2026-10-06T09:00:00", "다음날");
        create(createMember(), "2026-10-05T10:00:00", "다른 회원");

        mockMvc.perform(get("/api/memo-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].content").value("산책 30분"))
            .andExpect(jsonPath("$.result[1].content").value("저녁 늦게 먹음"));
    }

    @Test
    void 메모_기록을_수정하고_삭제한다() throws Exception {
        Member me = createMember();
        Integer id = create(me, "2026-10-05T09:00:00", "산책");

        mockMvc.perform(put("/api/memo-records/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordedAt\":\"2026-10-05T10:00:00\",\"content\":\"산책 1시간\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.recordedAt").value("2026-10-05T10:00:00"))
            .andExpect(jsonPath("$.result.content").value("산책 1시간"));

        mockMvc.perform(delete("/api/memo-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/memo-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEMO_RECORD_4040"));
    }

    @Test
    void 다른_회원의_메모_기록은_조회_수정_삭제할_수_없다() throws Exception {
        Integer id = create(createMember(), "2026-10-05T09:00:00", "산책");
        String other = bearer(createMember());

        mockMvc.perform(get("/api/memo-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/memo-records/{id}", id)
                .header("Authorization", other)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordedAt\":\"2026-10-05T09:00:00\",\"content\":\"수정\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/memo-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
    }

    @Test
    void 내용이_비었거나_1000자를_넘으면_400() throws Exception {
        Member me = createMember();

        for (String content : new String[] {"   ", "가".repeat(1001)}) {
            mockMvc.perform(post("/api/memo-records")
                    .header("Authorization", bearer(me))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"recordedAt\":\"2026-10-05T09:00:00\",\"content\":\"%s\"}".formatted(content)))
                .andExpect(status().isBadRequest());
        }
    }

    private Integer create(Member member, String recordedAt, String content) throws Exception {
        String body = mockMvc.perform(post("/api/memo-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recordedAt\":\"%s\",\"content\":\"%s\"}".formatted(recordedAt, content)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.memoRecordId");
    }
}
