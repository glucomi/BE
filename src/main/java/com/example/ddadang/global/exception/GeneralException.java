package com.example.ddadang.global.exception;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;

/**
 * 도메인에서 BaseCode(에러 상태)만 넘겨 던지는 공통 예외. GlobalExceptionHandler가
 * ApiResponse 실패 포맷으로 변환한다.
 */
@Getter
public class GeneralException extends RuntimeException {

    private final BaseCode code;

    public GeneralException(BaseCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
