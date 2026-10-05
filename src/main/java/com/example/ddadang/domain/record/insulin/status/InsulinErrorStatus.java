package com.example.ddadang.domain.record.insulin.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InsulinErrorStatus implements BaseCode {

    INSULIN_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "INSULIN_4040", "인슐린 제품을 찾을 수 없습니다."),
    MEMBER_INSULIN_NOT_FOUND(HttpStatus.NOT_FOUND, "INSULIN_4041", "등록된 내 인슐린을 찾을 수 없습니다."),
    INSULIN_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "INSULIN_4042", "인슐린 기록을 찾을 수 없습니다."),
    MEMBER_INSULIN_ALREADY_EXISTS(HttpStatus.CONFLICT, "INSULIN_4090", "이미 내 인슐린에 등록된 제품입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
