package com.example.ddadang.domain.member.dto.request;

import com.example.ddadang.domain.member.enums.DiabetesType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OnboardingRequest(
    @Schema(description = "STEP 01 당뇨 유형")
    @NotNull DiabetesType diabetesType,

    @Schema(description = "STEP 02 키(cm)", example = "168")
    @NotNull @DecimalMin("50.0") @DecimalMax("250.0") BigDecimal heightCm,

    @Schema(description = "STEP 03 몸무게(kg)", example = "47.7")
    @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("200.0") BigDecimal weightKg,

    @Schema(description = "STEP 04 목표 혈당 하한(mg/dL)", example = "70")
    @NotNull @Min(40) @Max(400) Integer targetGlucoseMin,

    @Schema(description = "STEP 04 목표 혈당 상한(mg/dL)", example = "180")
    @NotNull @Min(40) @Max(400) Integer targetGlucoseMax
) {
}
