package com.example.ddadang.domain.record.meal.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MealErrorStatus implements BaseCode {

    INVALID_INTAKE_UNIT(HttpStatus.BAD_REQUEST, "MEAL_4000", "섭취량 단위가 음식의 기준 단위와 다릅니다. 기준 단위나 SERVING(인분)을 사용해주세요."),
    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "MEAL_4040", "음식을 찾을 수 없습니다."),
    MEAL_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "MEAL_4041", "식사 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
