package com.example.ddadang.domain.record.exercise.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ExerciseRecordRequest(
    @NotNull Long exerciseId,
    @Schema(description = "운동 일시", example = "2026-10-05T19:00:00") @NotNull LocalDateTime performedAt,
    @Schema(description = "운동 시간(분, 1~1440)", example = "30") @NotNull @Min(1) @Max(1440) Integer durationMin,
    @Schema(description = "메모(선택, 최대 1000자)") @Size(max = 1000) String memo
) {
}
