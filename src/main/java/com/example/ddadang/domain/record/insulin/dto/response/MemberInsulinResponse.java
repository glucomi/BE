package com.example.ddadang.domain.record.insulin.dto.response;

import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.entity.MemberInsulin;
import com.example.ddadang.domain.record.insulin.enums.InsulinActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record MemberInsulinResponse(
    Long memberInsulinId,
    Long insulinProductId,
    String name,
    InsulinActionType actionType,
    @Schema(description = "기본 투여량(U)") BigDecimal defaultDoseUnit
) {

    public static MemberInsulinResponse from(MemberInsulin memberInsulin) {
        InsulinProduct product = memberInsulin.getInsulinProduct();
        return new MemberInsulinResponse(
            memberInsulin.getId(), product.getId(), product.getName(), product.getActionType(),
            memberInsulin.getDefaultDoseUnit()
        );
    }
}
