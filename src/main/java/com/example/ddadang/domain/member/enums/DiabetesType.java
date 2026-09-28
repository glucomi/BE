package com.example.ddadang.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MO-CURATION-010 STEP 01 선택지.
 */
@Getter
@RequiredArgsConstructor
public enum DiabetesType {
    TYPE1("1형 당뇨"),
    TYPE2_INSULIN("2형 당뇨(인슐린 투여)"),
    TYPE2_NO_INSULIN("2형 당뇨(인슐린 미투여)"),
    PRE("전 당뇨"),
    GESTATIONAL("임신성 당뇨"),
    NONE("해당 없음");

    private final String description;
}
