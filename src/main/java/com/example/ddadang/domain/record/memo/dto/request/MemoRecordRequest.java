package com.example.ddadang.domain.record.memo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MemoRecordRequest(
    @Schema(description = "기록 일시", example = "2026-10-05T21:00:00") @NotNull LocalDateTime recordedAt,
    @Schema(description = "내용(최대 1000자)", example = "오늘은 저녁을 늦게 먹었다") @NotBlank @Size(max = 1000) String content
) {
}
