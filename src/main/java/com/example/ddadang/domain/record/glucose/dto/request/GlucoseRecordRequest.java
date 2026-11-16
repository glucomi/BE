package com.example.ddadang.domain.record.glucose.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record GlucoseRecordRequest(
    @Schema(description = "측정 일시", example = "2026-10-05T08:00:00") @NotNull LocalDateTime measuredAt,
    @Schema(description = "혈당값(mg/dL, 10~600)", example = "110") @NotNull @Min(10) @Max(600) Integer valueMgDl,
    @Schema(description = "메모(선택, 최대 1000자)") @Size(max = 1000) String memo
) {
}
