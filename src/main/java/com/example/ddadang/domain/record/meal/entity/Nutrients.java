package com.example.ddadang.domain.record.meal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기준 제공량당 영양 성분(MO-MEAL-050 직접 등록 항목). 열량 외에는 값이 없을 수 있다.
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Nutrients {

    @Column(name = "kcal", nullable = false, precision = 8, scale = 2)
    private BigDecimal kcal;

    @Column(name = "carbohydrate_g", precision = 8, scale = 2)
    private BigDecimal carbohydrateG;

    @Column(name = "sugars_g", precision = 8, scale = 2)
    private BigDecimal sugarsG;

    @Column(name = "dietary_fiber_g", precision = 8, scale = 2)
    private BigDecimal dietaryFiberG;

    @Column(name = "protein_g", precision = 8, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "fat_g", precision = 8, scale = 2)
    private BigDecimal fatG;

    @Column(name = "saturated_fat_g", precision = 8, scale = 2)
    private BigDecimal saturatedFatG;

    @Column(name = "trans_fat_g", precision = 8, scale = 2)
    private BigDecimal transFatG;

    @Column(name = "fatty_acid_g", precision = 8, scale = 2)
    private BigDecimal fattyAcidG;

    @Column(name = "unsaturated_fat_g", precision = 8, scale = 2)
    private BigDecimal unsaturatedFatG;

    @Column(name = "cholesterol_mg", precision = 8, scale = 2)
    private BigDecimal cholesterolMg;

    @Column(name = "sodium_mg", precision = 8, scale = 2)
    private BigDecimal sodiumMg;

    @Column(name = "caffeine_mg", precision = 8, scale = 2)
    private BigDecimal caffeineMg;

    /**
     * 섭취량 비율(섭취량 / 기준 제공량)만큼 모든 성분을 환산한다.
     */
    public Nutrients scale(BigDecimal ratio) {
        Function<BigDecimal, BigDecimal> apply = value ->
            value == null ? null : value.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        return new Nutrients(
            apply.apply(kcal), apply.apply(carbohydrateG), apply.apply(sugarsG), apply.apply(dietaryFiberG),
            apply.apply(proteinG), apply.apply(fatG), apply.apply(saturatedFatG), apply.apply(transFatG),
            apply.apply(fattyAcidG), apply.apply(unsaturatedFatG), apply.apply(cholesterolMg),
            apply.apply(sodiumMg), apply.apply(caffeineMg)
        );
    }
}
