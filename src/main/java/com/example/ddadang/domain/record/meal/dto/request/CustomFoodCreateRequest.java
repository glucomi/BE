package com.example.ddadang.domain.record.meal.dto.request;

import com.example.ddadang.domain.record.meal.dto.NutrientsDto;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CustomFoodCreateRequest(
    @Schema(description = "브랜드명(선택)") @Size(max = 100) String brand,
    @Schema(description = "음식명") @NotBlank @Size(max = 100) String name,
    @Schema(description = "기준 섭취량", example = "200") @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal servingAmount,
    @Schema(description = "섭취량 단위") @NotNull FoodUnit servingUnit,
    @Schema(description = "기준 섭취량당 영양 성분") @NotNull @Valid NutrientsDto nutrients
) {
}
