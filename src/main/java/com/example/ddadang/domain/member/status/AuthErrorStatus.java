package com.example.ddadang.domain.member.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorStatus implements BaseCode {

    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4010", "카카오 토큰이 유효하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4011", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4012", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4013", "유효하지 않은 refresh token입니다. 다시 로그인해주세요."),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "AUTH_4030", "탈퇴한 회원입니다."),
    KAKAO_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AUTH_5020", "카카오 서버 응답에 실패했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
