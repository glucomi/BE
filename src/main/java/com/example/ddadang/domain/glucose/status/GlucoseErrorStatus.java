package com.example.ddadang.domain.glucose.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlucoseErrorStatus implements BaseCode {

    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "GLUCOSE_4000", "조회 기간이 올바르지 않습니다. (from ≤ to, 최대 42일)"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
