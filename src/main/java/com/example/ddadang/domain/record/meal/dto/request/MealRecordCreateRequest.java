package com.example.ddadang.domain.record.meal.dto.request;

import com.example.ddadang.domain.record.meal.enums.IntakeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MealRecordCreateRequest(
    @Schema(description = "식사 일시", example = "2026-09-28T12:30:00") @NotNull LocalDateTime eatenAt,
    @Schema(description = "메모(선택, 최대 1000자)") @Size(max = 1000) String memo,
    @Schema(description = "담은 음식 목록") @NotEmpty @Size(max = 30) @Valid List<Item> items
) {

    public record Item(
        @NotNull Long foodId,
        @Schema(description = "섭취량", example = "200") @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
        @Schema(description = "G/ML(음식 기준 단위와 동일해야 함) 또는 SERVING(인분)") @NotNull IntakeUnit unit
    ) {
    }
}
