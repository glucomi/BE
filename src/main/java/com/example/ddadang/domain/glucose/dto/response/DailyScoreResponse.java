package com.example.ddadang.domain.glucose.dto.response;

import com.example.ddadang.domain.glucose.score.GlucoseScoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record DailyScoreResponse(
    LocalDate date,
    @Schema(description = "혈당 점수(표시용 정수). status가 FINAL/PROVISIONAL일 때만 값이 있음") Integer score,
    GlucoseScoreStatus status,
    @Schema(description = "확정 저장된 점수인지. true면 이후 데이터가 들어와도 바뀌지 않음") boolean frozen
) {
}
