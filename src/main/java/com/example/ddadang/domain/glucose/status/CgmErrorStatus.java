package com.example.ddadang.domain.glucose.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CgmErrorStatus implements BaseCode {

    INVALID_EVENT_TYPE(HttpStatus.BAD_REQUEST, "CGM_4000", "type 값을 확인해주세요."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "CGM_4001",
        "state 값이 유효하지 않습니다. 연결을 처음부터 다시 시도해주세요."),
    CGM_REAUTH_REQUIRED(HttpStatus.BAD_REQUEST, "CGM_4002", "CGM 토큰을 갱신할 수 없습니다. CGM 연결을 다시 진행해주세요."),
    CGM_NOT_CONNECTED(HttpStatus.NOT_FOUND, "CGM_4040", "연결된 CGM이 없습니다."),
    CGM_ALREADY_LINKED(HttpStatus.CONFLICT, "CGM_4090", "이미 다른 회원에게 연결된 CGM 계정입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
