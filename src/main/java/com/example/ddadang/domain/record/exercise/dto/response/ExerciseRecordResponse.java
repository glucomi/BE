package com.example.ddadang.domain.record.exercise.dto.response;

import com.example.ddadang.domain.record.exercise.entity.ExerciseRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExerciseRecordResponse(
    Long exerciseRecordId,
    Long exerciseId,
    @Schema(description = "운동명") String name,
    LocalDateTime performedAt,
    Integer durationMin,
    @Schema(description = "소모 열량(kcal). 체중 기록이 하나도 없으면 null") BigDecimal kcal,
    String memo
) {

    public static ExerciseRecordResponse from(ExerciseRecord record) {
        return new ExerciseRecordResponse(
            record.getId(), record.getExercise().getId(), record.getExercise().getName(), record.getPerformedAt(),
            record.getDurationMin(), record.getKcal(), record.getMemo()
        );
    }
}
