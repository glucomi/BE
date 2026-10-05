package com.example.ddadang.domain.record.weight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeightRecordRequest(
    @Schema(description = "측정 일시", example = "2026-10-05T07:00:00") @NotNull LocalDateTime measuredAt,
    @Schema(description = "체중(kg, 20~300, 소수 1자리)", example = "47.7")
    @NotNull @DecimalMin("20.0") @DecimalMax("300.0") @Digits(integer = 3, fraction = 1) BigDecimal weightKg
) {
}
