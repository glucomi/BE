package com.example.ddadang.domain.cgm.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CgmErrorStatus implements BaseCode {

    INVALID_EVENT_TYPE(HttpStatus.BAD_REQUEST, "CGM_4000", "type 값을 확인해주세요."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "CGM_4001",
        "state 값이 일치하지 않습니다. CSRF 공격이 의심되거나 세션이 만료되었습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
