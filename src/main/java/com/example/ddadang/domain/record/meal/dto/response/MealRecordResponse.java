package com.example.ddadang.domain.record.meal.dto.response;

import com.example.ddadang.domain.record.meal.dto.NutrientsDto;
import com.example.ddadang.domain.record.meal.entity.MealRecord;
import com.example.ddadang.domain.record.meal.entity.MealRecordItem;
import com.example.ddadang.domain.record.meal.entity.Nutrients;
import com.example.ddadang.domain.record.meal.enums.IntakeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record MealRecordResponse(
    Long mealRecordId,
    LocalDateTime eatenAt,
    String memo,
    List<Item> items,
    @Schema(description = "메뉴 합계") Total total
) {

    public static MealRecordResponse from(MealRecord record) {
        List<Item> items = record.getItems().stream().map(Item::from).toList();
        return new MealRecordResponse(record.getId(), record.getEatenAt(), record.getMemo(), items, Total.of(record));
    }

    public record Item(
        Long mealRecordItemId,
        Long foodId,
        String name,
        String brand,
        BigDecimal amount,
        IntakeUnit unit,
        @Schema(description = "섭취량 기준으로 환산된 영양 성분") NutrientsDto nutrients
    ) {

        static Item from(MealRecordItem item) {
            return new Item(
                item.getId(), item.getFood().getId(), item.getFood().getName(), item.getFood().getBrand(),
                item.getAmount(), item.getUnit(), NutrientsDto.from(item.getNutrients())
            );
        }
    }

    public record Total(BigDecimal kcal, BigDecimal carbohydrateG, BigDecimal proteinG, BigDecimal fatG) {

        static Total of(MealRecord record) {
            List<Nutrients> list = record.getItems().stream().map(MealRecordItem::getNutrients).toList();
            return new Total(
                sum(list, Nutrients::getKcal), sum(list, Nutrients::getCarbohydrateG),
                sum(list, Nutrients::getProteinG), sum(list, Nutrients::getFatG)
            );
        }

        private static BigDecimal sum(List<Nutrients> list, Function<Nutrients, BigDecimal> getter) {
            return list.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
