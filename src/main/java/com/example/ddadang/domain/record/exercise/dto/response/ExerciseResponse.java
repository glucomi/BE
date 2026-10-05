package com.example.ddadang.domain.record.exercise.dto.response;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import java.math.BigDecimal;

public record ExerciseResponse(
    Long exerciseId,
    String name,
    BigDecimal met,
    boolean popular
) {

    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getMet(), exercise.isPopular());
    }
}
