package com.example.ddadang.domain.record.insulin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsulinRecordRequest(
    @Schema(description = "내 인슐린 ID") @NotNull Long memberInsulinId,
    @Schema(description = "투여 일시", example = "2026-10-05T08:00:00") @NotNull LocalDateTime injectedAt,
    @Schema(description = "투여량(U, 0 초과, 소수 1자리)", example = "10")
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal doseUnit,
    @Schema(description = "메모(선택, 최대 1000자)") @Size(max = 1000) String memo
) {
}
