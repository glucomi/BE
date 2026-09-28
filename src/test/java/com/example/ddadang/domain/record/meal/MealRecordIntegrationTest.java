package com.example.ddadang.domain.record.meal;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class MealRecordIntegrationTest extends IntegrationTest {

    @Autowired
    private FoodRepository foodRepository;

    @Test
    void 섭취량_기준으로_영양성분을_환산해_저장한다() throws Exception {
        Member me = createMember();
        Food rice = food("흑미밥", "210", FoodUnit.G, "315", "69.3");
        Food milk = food("우유", "200", FoodUnit.ML, "130", "10");

        String body = mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"eatenAt":"2026-09-28T12:30:00","memo":"점심",
                     "items":[
                       {"foodId":%d,"amount":105,"unit":"G"},
                       {"foodId":%d,"amount":2,"unit":"SERVING"}
                     ]}
                    """.formatted(rice.getId(), milk.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.items.length()").value(2))
            .andExpect(jsonPath("$.result.items[0].nutrients.kcal").value(157.5))
            .andExpect(jsonPath("$.result.items[0].nutrients.carbohydrateG").value(34.65))
            .andExpect(jsonPath("$.result.items[1].nutrients.kcal").value(260.0))
            .andExpect(jsonPath("$.result.total.kcal").value(417.5))
            .andExpect(jsonPath("$.result.total.carbohydrateG").value(54.65))
            .andReturn().getResponse().getContentAsString();

        Integer mealRecordId = JsonPath.read(body, "$.result.mealRecordId");
        mockMvc.perform(get("/api/meal-records/{id}", mealRecordId).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.memo").value("점심"))
            .andExpect(jsonPath("$.result.items[0].name").value("흑미밥"));
        mockMvc.perform(get("/api/meal-records/{id}", mealRecordId).header("Authorization", bearer(createMember())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEAL_4041"));
    }

    @Test
    void 음식_기준단위와_다른_단위면_400() throws Exception {
        Member me = createMember();
        Food rice = food("현미밥", "210", FoodUnit.G, "330", "71");

        mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"eatenAt":"2026-09-28T12:30:00","items":[{"foodId":%d,"amount":100,"unit":"ML"}]}
                    """.formatted(rice.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("MEAL_4000"));
    }

    @Test
    void 담은_음식이_없으면_400() throws Exception {
        Member me = createMember();

        mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eatenAt\":\"2026-09-28T12:30:00\",\"items\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 최근_먹은_음식은_최근에_먹은_순으로_중복없이_조회된다() throws Exception {
        Member me = createMember();
        Food a = food("사과", "200", FoodUnit.G, "106", "28");
        Food b = food("바나나", "100", FoodUnit.G, "93", "22");

        record(me, "2026-09-26T08:00:00", a);
        record(me, "2026-09-27T08:00:00", b);
        record(me, "2026-09-28T08:00:00", a);

        mockMvc.perform(get("/api/foods/recent").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(2))
            .andExpect(jsonPath("$.result.content[0].name").value("사과"))
            .andExpect(jsonPath("$.result.content[1].name").value("바나나"));
        mockMvc.perform(get("/api/foods/recent").header("Authorization", bearer(createMember())))
            .andExpect(jsonPath("$.result.totalElements").value(0));
    }

    private void record(Member member, String eatenAt, Food food) throws Exception {
        mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"eatenAt":"%s","items":[{"foodId":%d,"amount":1,"unit":"SERVING"}]}
                    """.formatted(eatenAt, food.getId())))
            .andExpect(status().isCreated());
    }

    private Food food(String name, String amount, FoodUnit unit, String kcal, String carb) {
        return foodRepository.save(Food.dbFood(
            name, null, FoodCategory.GENERAL, new BigDecimal(amount), unit,
            Nutrients.builder().kcal(new BigDecimal(kcal)).carbohydrateG(new BigDecimal(carb)).build()
        ));
    }
}
