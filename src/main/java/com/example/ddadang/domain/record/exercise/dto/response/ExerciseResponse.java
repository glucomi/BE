package com.example.ddadang.domain.record.exercise.dto.response;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record ExerciseResponse(
    Long exerciseId,
    String name,
    BigDecimal met,
    boolean popular,
    @Schema(description = "30분 기준 소모 열량(kcal, 회원 최근 체중 기준). 체중 기록이 없으면 null")
    BigDecimal kcalPer30Min
) {

    private static final int REFERENCE_MINUTES = 30;

    public static ExerciseResponse of(Exercise exercise, BigDecimal weightKg) {
        return new ExerciseResponse(
            exercise.getId(), exercise.getName(), exercise.getMet(), exercise.isPopular(),
            exercise.kcalFor(weightKg, REFERENCE_MINUTES)
        );
    }
}
