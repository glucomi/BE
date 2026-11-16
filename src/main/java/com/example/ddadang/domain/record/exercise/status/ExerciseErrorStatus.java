package com.example.ddadang.domain.record.exercise.status;

import com.example.ddadang.global.response.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExerciseErrorStatus implements BaseCode {

    EXERCISE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXERCISE_4040", "운동을 찾을 수 없습니다."),
    EXERCISE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "EXERCISE_4041", "운동 기록을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
