package com.example.ddadang.domain.record.meal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodCategory {
    GENERAL("일반 식품"),
    PROCESSED("가공 식품");

    private final String description;
}
