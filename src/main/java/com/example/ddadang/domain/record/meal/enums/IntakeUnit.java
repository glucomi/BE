package com.example.ddadang.domain.record.meal.enums;

/**
 * 식사 기록 시 섭취량 단위. G/ML은 음식의 기준 단위(servingUnit)와 같아야 하고,
 * SERVING은 기준 제공량(servingAmount) 1회분을 1로 본다.
 */
public enum IntakeUnit {
    G, ML, SERVING;

    public boolean matches(FoodUnit foodUnit) {
        return this == SERVING || this.name().equals(foodUnit.name());
    }
}
