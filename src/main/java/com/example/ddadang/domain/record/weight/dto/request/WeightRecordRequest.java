package com.example.ddadang.domain.record.weight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WeightRecordRequest(
    @Schema(description = "체중(kg, 0 초과 200 이하, 0.1kg 단위)", example = "47.7")
    @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("200.0") @Digits(integer = 3, fraction = 1)
    BigDecimal weightKg
) {
}
