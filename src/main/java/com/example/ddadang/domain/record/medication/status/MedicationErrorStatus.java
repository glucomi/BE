package com.example.ddadang.domain.record.medication.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MedicationErrorStatus implements BaseCode {

    MEMBER_MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEDICATION_4040", "등록된 내 복용약을 찾을 수 없습니다."),
    MEDICATION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "MEDICATION_4041", "복약 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
