package com.example.ddadang.domain.record.meal.dto.response;

import com.example.ddadang.domain.record.meal.dto.NutrientsDto;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record FoodDetailResponse(
    Long foodId,
    FoodSource source,
    FoodCategory category,
    String name,
    String brand,
    @Schema(description = "기준 섭취량. nutrients는 이 양 기준") BigDecimal servingAmount,
    FoodUnit servingUnit,
    NutrientsDto nutrients,
    @Schema(description = "예상 혈당 변화 등급(A+/A/B+/B/C/F). 예측 로직 확정 전까지 null") String glucoseGrade,
    boolean favorite
) {

    public static FoodDetailResponse of(Food food, boolean favorite) {
        return new FoodDetailResponse(
            food.getId(), food.getSource(), food.getCategory(), food.getName(), food.getBrand(),
            food.getServingAmount(), food.getServingUnit(), NutrientsDto.from(food.getNutrients()),
            food.getGlucoseGrade() == null ? null : food.getGlucoseGrade().getLabel(),
            favorite
        );
    }
}
