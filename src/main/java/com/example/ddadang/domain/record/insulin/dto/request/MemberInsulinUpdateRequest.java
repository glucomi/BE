package com.example.ddadang.domain.record.insulin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MemberInsulinUpdateRequest(
    @Schema(description = "기본 투여량(U, 0 초과, 소수 1자리)", example = "12")
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 3, fraction = 1) BigDecimal defaultDoseUnit
) {
}
