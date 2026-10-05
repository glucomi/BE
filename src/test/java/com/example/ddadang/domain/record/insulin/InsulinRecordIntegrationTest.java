package com.example.ddadang.domain.record.insulin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.enums.InsulinActionType;
import com.example.ddadang.domain.record.insulin.repository.InsulinProductRepository;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class InsulinRecordIntegrationTest extends IntegrationTest {

    @Autowired
    private InsulinProductRepository insulinProductRepository;

    @Test
    void 기본_인슐린_제품_목록이_적재되고_제품명으로_검색된다() throws Exception {
        mockMvc.perform(get("/api/insulin-products").header("Authorization", bearer(createMember())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(insulinProductRepository.count()))
            .andExpect(jsonPath("$.result[0].actionType").value("RAPID"));
        mockMvc.perform(get("/api/insulin-products").param("keyword", "란투스").header("Authorization", bearer(createMember())))
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].name").value("란투스 솔로스타"))
            .andExpect(jsonPath("$.result[0].actionType").value("LONG"));
    }

    @Test
    void 내_인슐린을_등록_수정_삭제한다() throws Exception {
        Member me = createMember();
        InsulinProduct product = product("테스트 지속형", InsulinActionType.LONG);
        Integer id = addMyInsulin(me, product, "10");

        mockMvc.perform(post("/api/members/me/insulins")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"insulinProductId\":%d,\"defaultDoseUnit\":8}".formatted(product.getId())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INSULIN_4090"));

        mockMvc.perform(put("/api/members/me/insulins/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"defaultDoseUnit\":12.5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.defaultDoseUnit").value(12.5));
        mockMvc.perform(get("/api/members/me/insulins").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].name").value("테스트 지속형"));

        mockMvc.perform(delete("/api/members/me/insulins/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/me/insulins").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.length()").value(0));
        addMyInsulin(me, product, "10");
    }

    @Test
    void 인슐린_기록을_등록하고_날짜별로_시간순_조회한다() throws Exception {
        Member me = createMember();
        Integer rapid = addMyInsulin(me, product("테스트 초속효성", InsulinActionType.RAPID), "6");
        Integer basal = addMyInsulin(me, product("테스트 지속형", InsulinActionType.LONG), "10");
        record(me, basal, "2026-10-05T22:00:00", "10");
        record(me, rapid, "2026-10-05T08:00:00", "6.5");
        record(me, rapid, "2026-10-06T08:00:00", "6");

        mockMvc.perform(get("/api/insulin-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].name").value("테스트 초속효성"))
            .andExpect(jsonPath("$.result[0].actionType").value("RAPID"))
            .andExpect(jsonPath("$.result[0].doseUnit").value(6.5))
            .andExpect(jsonPath("$.result[1].memberInsulinId").value(basal));
    }

    @Test
    void 내_인슐린을_삭제해도_기존_기록은_조회되고_새_기록에는_쓸_수_없다() throws Exception {
        Member me = createMember();
        Integer deleted = addMyInsulin(me, product("테스트 초속효성", InsulinActionType.RAPID), "6");
        Integer active = addMyInsulin(me, product("테스트 지속형", InsulinActionType.LONG), "10");
        Integer recordId = record(me, deleted, "2026-10-05T08:00:00", "6");
        mockMvc.perform(delete("/api/members/me/insulins/{id}", deleted).header("Authorization", bearer(me)));

        mockMvc.perform(get("/api/insulin-records/{id}", recordId).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.name").value("테스트 초속효성"));
        mockMvc.perform(post("/api/insulin-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(deleted, "2026-10-05T12:00:00", "6")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INSULIN_4041"));

        // 삭제된 인슐린 그대로 두고 투여량만 수정하는 건 가능
        mockMvc.perform(put("/api/insulin-records/{id}", recordId)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(deleted, "2026-10-05T08:00:00", "7")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.doseUnit").value(7.0));
        mockMvc.perform(put("/api/insulin-records/{id}", recordId)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(active, "2026-10-05T08:00:00", "10")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.name").value("테스트 지속형"));
    }

    @Test
    void 다른_회원의_인슐린과_기록에는_접근할_수_없다() throws Exception {
        Member owner = createMember();
        Integer insulinId = addMyInsulin(owner, product("테스트 초속효성", InsulinActionType.RAPID), "6");
        Integer recordId = record(owner, insulinId, "2026-10-05T08:00:00", "6");
        Member other = createMember();

        mockMvc.perform(post("/api/insulin-records")
                .header("Authorization", bearer(other))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(insulinId, "2026-10-05T08:00:00", "6")))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/members/me/insulins/{id}", insulinId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/insulin-records/{id}", recordId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INSULIN_4042"));
        mockMvc.perform(delete("/api/insulin-records/{id}", recordId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/insulin-records/{id}", recordId).header("Authorization", bearer(owner)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/insulin-records/{id}", recordId).header("Authorization", bearer(owner)))
            .andExpect(status().isNotFound());
    }

    private InsulinProduct product(String name, InsulinActionType actionType) {
        return insulinProductRepository.save(new InsulinProduct(name, actionType));
    }

    private Integer addMyInsulin(Member member, InsulinProduct product, String dose) throws Exception {
        String body = mockMvc.perform(post("/api/members/me/insulins")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"insulinProductId\":%d,\"defaultDoseUnit\":%s}".formatted(product.getId(), dose)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.memberInsulinId");
    }

    private Integer record(Member member, Integer memberInsulinId, String injectedAt, String dose) throws Exception {
        String body = mockMvc.perform(post("/api/insulin-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson(memberInsulinId, injectedAt, dose)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.insulinRecordId");
    }

    private String recordJson(Integer memberInsulinId, String injectedAt, String dose) {
        return "{\"memberInsulinId\":%d,\"injectedAt\":\"%s\",\"doseUnit\":%s}".formatted(memberInsulinId, injectedAt, dose);
    }
}
