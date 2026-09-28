package com.example.ddadang.domain.cgm.util;

import java.util.Arrays;

/**
 * GET /v1/public/events가 지원하는 7개 이벤트 타입(개발가이드 1. 개요).
 */
public enum CgmEventType {
    BGM, EXERCISE, INSULIN, KETONE, MEAL, MEDICINE, MEMO;

    public static boolean isValid(String value) {
        return Arrays.stream(values()).anyMatch(type -> type.name().equalsIgnoreCase(value));
    }
}
