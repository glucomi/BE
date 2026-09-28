package com.example.ddadang.domain.record.meal.dto.response;

import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "음식 목록 항목 (예: 일반 식품 / 흑미밥 150g·122kcal)")
public record FoodSummaryResponse(
    Long foodId,
    @Schema(description = "DB: 음식 DB, CUSTOM: 직접 등록") FoodSource source,
    @Schema(description = "GENERAL: 일반 식품, PROCESSED: 가공 식품 (CUSTOM이면 null)") FoodCategory category,
    String name,
    String brand,
    BigDecimal servingAmount,
    FoodUnit servingUnit,
    BigDecimal kcal,
    boolean favorite
) {

    public static FoodSummaryResponse of(Food food, boolean favorite) {
        return new FoodSummaryResponse(
            food.getId(), food.getSource(), food.getCategory(), food.getName(), food.getBrand(),
            food.getServingAmount(), food.getServingUnit(), food.getNutrients().getKcal(), favorite
        );
    }
}
