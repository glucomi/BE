package com.example.ddadang.domain.member.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorStatus implements BaseCode {

    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "MEMBER_4000", "필수 약관에 모두 동의해야 합니다."),
    DUPLICATED_TERMS(HttpStatus.BAD_REQUEST, "MEMBER_4001", "같은 약관이 중복으로 전달되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_4040", "회원을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
