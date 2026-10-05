package com.example.ddadang.domain.record.weight.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WeightRecordErrorStatus implements BaseCode {

    WEIGHT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "WEIGHT_RECORD_4040", "체중 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
