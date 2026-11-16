package com.example.ddadang.domain.record.memo.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemoRecordErrorStatus implements BaseCode {

    MEMO_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_RECORD_4040", "메모 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
