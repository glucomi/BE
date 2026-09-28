package com.example.ddadang.domain.cgm.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CgmSuccessStatus implements BaseCode {

    EVENT_EMPTY(HttpStatus.OK, "CGM_2001", "해당 카테고리에 값이 아직 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
