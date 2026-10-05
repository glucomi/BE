package com.example.ddadang.domain.record.medication;

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

class MedicationRecordIntegrationTest extends IntegrationTest {

    @Test
    void 내_복용약을_등록_수정_삭제한다() throws Exception {
        Member me = createMember();
        Integer id = addMyMedication(me, "DIABETES", "\"다이아벡스정\"");
        addMyMedication(me, "SUPPLEMENT", "\"  \"");

        mockMvc.perform(get("/api/members/me/medications").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].productName").value("다이아벡스정"))
            .andExpect(jsonPath("$.result[1].category").value("SUPPLEMENT"))
            .andExpect(jsonPath("$.result[1].productName").doesNotExist());

        mockMvc.perform(put("/api/members/me/medications/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"HYPERTENSION\",\"productName\":\"노바스크정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.category").value("HYPERTENSION"))
            .andExpect(jsonPath("$.result.productName").value("노바스크정"));

        mockMvc.perform(delete("/api/members/me/medications/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/me/medications").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void 복약_기록을_등록하고_날짜별로_시간순_조회한다() throws Exception {
        Member me = createMember();
        Integer diabetes = addMyMedication(me, "DIABETES", "\"다이아벡스정\"");
        Integer supplement = addMyMedication(me, "SUPPLEMENT", "null");
        record(me, supplement, "2026-10-05T21:00:00", "자기 전");
        record(me, diabetes, "2026-10-05T08:30:00", null);
        record(me, diabetes, "2026-10-04T08:30:00", null);

        mockMvc.perform(get("/api/medication-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].category").value("DIABETES"))
            .andExpect(jsonPath("$.result[0].productName").value("다이아벡스정"))
            .andExpect(jsonPath("$.result[1].memberMedicationId").value(supplement))
            .andExpect(jsonPath("$.result[1].memo").value("자기 전"));
    }

    @Test
    void 내_복용약을_삭제해도_기존_기록은_조회되고_새_기록에는_쓸_수_없다() throws Exception {
        Member me = createMember();
        Integer deleted = addMyMedication(me, "DIABETES", "\"다이아벡스정\"");
        Integer active = addMyMedication(me, "HYPERTENSION", "null");
        Integer recordId = record(me, deleted, "2026-10-05T08:30:00", null);
        mockMvc.perform(delete("/api/members/me/medications/{id}", deleted).header("Authorization", bearer(me)));

        mockMvc.perform(get("/api/medication-records/{id}", recordId).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.productName").value("다이아벡스정"));
        mockMvc.perform(post("/api/medication-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(deleted, "2026-10-05T20:00:00", null)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEDICATION_4040"));

        mockMvc.perform(put("/api/medication-records/{id}", recordId)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(deleted, "2026-10-05T09:00:00", "늦게 먹음")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.takenAt").value("2026-10-05T09:00:00"));
        mockMvc.perform(put("/api/medication-records/{id}", recordId)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(active, "2026-10-05T09:00:00", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.category").value("HYPERTENSION"));
    }

    @Test
    void 다른_회원의_복용약과_기록에는_접근할_수_없다() throws Exception {
        Member owner = createMember();
        Integer medicationId = addMyMedication(owner, "DIABETES", "null");
        Integer recordId = record(owner, medicationId, "2026-10-05T08:30:00", null);
        Member other = createMember();

        mockMvc.perform(post("/api/medication-records")
                .header("Authorization", bearer(other))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(medicationId, "2026-10-05T08:30:00", null)))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/members/me/medications/{id}", medicationId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/medication-records/{id}", recordId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEDICATION_4041"));
        mockMvc.perform(delete("/api/medication-records/{id}", recordId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/medication-records/{id}", recordId).header("Authorization", bearer(owner)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/medication-records/{id}", recordId).header("Authorization", bearer(owner)))
            .andExpect(status().isNotFound());
    }

    @Test
    void 약_종류가_없거나_잘못되면_400() throws Exception {
        Member me = createMember();

        for (String body : new String[] {"{\"productName\":\"약\"}", "{\"category\":\"VITAMIN\"}"}) {
            mockMvc.perform(post("/api/members/me/medications")
                    .header("Authorization", bearer(me))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }
    }

    private Integer addMyMedication(Member member, String category, String productNameJson) throws Exception {
        String body = mockMvc.perform(post("/api/members/me/medications")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"%s\",\"productName\":%s}".formatted(category, productNameJson)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.memberMedicationId");
    }

    private Integer record(Member member, Integer memberMedicationId, String takenAt, String memo) throws Exception {
        String body = mockMvc.perform(post("/api/medication-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(memberMedicationId, takenAt, memo)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.medicationRecordId");
    }

    private String recordJson(Integer memberMedicationId, String takenAt, String memo) {
        String memoJson = memo == null ? "null" : "\"" + memo + "\"";
        return "{\"memberMedicationId\":%d,\"takenAt\":\"%s\",\"memo\":%s}".formatted(memberMedicationId, takenAt, memoJson);
    }
}
