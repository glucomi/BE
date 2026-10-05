package com.example.ddadang.domain.record.glucose.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlucoseRecordErrorStatus implements BaseCode {

    GLUCOSE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "GLUCOSE_RECORD_4040", "혈당 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
