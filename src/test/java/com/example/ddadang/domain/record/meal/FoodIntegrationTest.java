package com.example.ddadang.domain.record.meal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.Nutrients;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import com.example.ddadang.domain.record.meal.repository.FoodRepository;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class FoodIntegrationTest extends IntegrationTest {

    @Autowired
    private FoodRepository foodRepository;

    @Test
    void 검색은_DB음식과_내_직접등록_음식만_포함하고_짧은_이름순으로_정렬된다() throws Exception {
        String keyword = uniqueKeyword();
        Member me = createMember();
        Member other = createMember();
        dbFood(keyword + "볶음밥");
        dbFood(keyword);
        createCustomFood(me, keyword + "내음식");
        createCustomFood(other, keyword + "남의음식");

        mockMvc.perform(get("/api/foods").param("keyword", keyword).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(3))
            .andExpect(jsonPath("$.result.content[0].name").value(keyword))
            .andExpect(jsonPath("$.result.content[0].source").value("DB"))
            .andExpect(jsonPath("$.result.content[0].favorite").value(false));
    }

    @Test
    void 검색어가_비어있으면_400() throws Exception {
        Member me = createMember();

        mockMvc.perform(get("/api/foods").param("keyword", " ").header("Authorization", bearer(me)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    @Test
    void 즐겨찾기_추가_해제는_멱등이고_목록과_검색결과에_반영된다() throws Exception {
        String keyword = uniqueKeyword();
        Member me = createMember();
        Food food = dbFood(keyword);

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/foods/{id}/favorite", food.getId()).header("Authorization", bearer(me)))
                .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/foods/favorites").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].favorite").value(true));
        mockMvc.perform(get("/api/foods").param("keyword", keyword).header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.content[0].favorite").value(true));
        mockMvc.perform(get("/api/foods/{id}", food.getId()).header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.favorite").value(true));

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(delete("/api/foods/{id}/favorite", food.getId()).header("Authorization", bearer(me)))
                .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/foods/favorites").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.totalElements").value(0));
    }

    @Test
    void 직접_등록한_음식은_내_목록에만_보이고_다른_회원은_상세조회시_404() throws Exception {
        Member me = createMember();
        Member other = createMember();

        String body = createCustomFood(me, "단백질바");
        Integer foodId = JsonPath.read(body, "$.result.foodId");

        mockMvc.perform(get("/api/foods/custom").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.totalElements").value(1))
            .andExpect(jsonPath("$.result.content[0].source").value("CUSTOM"))
            .andExpect(jsonPath("$.result.content[0].brand").value("따당"));
        mockMvc.perform(get("/api/foods/{id}", foodId).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.nutrients.kcal").value(312))
            .andExpect(jsonPath("$.result.nutrients.sodiumMg").value(120));
        mockMvc.perform(get("/api/foods/{id}", foodId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEAL_4040"));
        mockMvc.perform(post("/api/foods/{id}/favorite", foodId).header("Authorization", bearer(other)))
            .andExpect(status().isNotFound());
    }

    @Test
    void 직접_등록시_열량이_없으면_400() throws Exception {
        Member me = createMember();

        mockMvc.perform(post("/api/foods/custom")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"물\",\"servingAmount\":500,\"servingUnit\":\"ML\",\"nutrients\":{}}"))
            .andExpect(status().isBadRequest());
    }

    private String createCustomFood(Member member, String name) throws Exception {
        return mockMvc.perform(post("/api/foods/custom")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"brand":"따당","name":"%s","servingAmount":70,"servingUnit":"G",
                     "nutrients":{"kcal":312,"carbohydrateG":30,"proteinG":20,"fatG":10,"sodiumMg":120}}
                    """.formatted(name)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
    }

    private Food dbFood(String name) {
        return foodRepository.save(Food.dbFood(
            name, null, FoodCategory.GENERAL, new BigDecimal("210"), FoodUnit.G,
            Nutrients.builder().kcal(new BigDecimal("315")).carbohydrateG(new BigDecimal("69.7")).build()
        ));
    }

    private String uniqueKeyword() {
        return "음식" + UUID.randomUUID().toString().substring(0, 6);
    }
}
