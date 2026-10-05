package com.example.ddadang.domain.record.insulin.dto.response;

import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.enums.InsulinActionType;
import io.swagger.v3.oas.annotations.media.Schema;

public record InsulinProductResponse(
    Long insulinProductId,
    String name,
    @Schema(description = "RAPID 초속효성 / SHORT 속효성 / INTERMEDIATE 중간형 / LONG 지속형 / MIXED 혼합형")
    InsulinActionType actionType
) {

    public static InsulinProductResponse from(InsulinProduct product) {
        return new InsulinProductResponse(product.getId(), product.getName(), product.getActionType());
    }
}
