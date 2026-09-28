package com.example.ddadang.domain.record.meal.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 음식 상세의 예상 혈당 변화 등급. 예측 로직 확정 전까지는 값이 비어 있다.
 */
@Getter
@RequiredArgsConstructor
public enum GlucoseGrade {
    A_PLUS("A+"),
    A("A"),
    B_PLUS("B+"),
    B("B"),
    C("C"),
    F("F");

    private final String label;
}
