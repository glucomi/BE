package com.example.ddadang.domain.record.insulin.dto.response;

import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.entity.InsulinRecord;
import com.example.ddadang.domain.record.insulin.enums.InsulinActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsulinRecordResponse(
    Long insulinRecordId,
    Long memberInsulinId,
    @Schema(description = "인슐린 제품명") String name,
    InsulinActionType actionType,
    LocalDateTime injectedAt,
    @Schema(description = "투여량(U)") BigDecimal doseUnit,
    String memo
) {

    public static InsulinRecordResponse from(InsulinRecord record) {
        InsulinProduct product = record.getMemberInsulin().getInsulinProduct();
        return new InsulinRecordResponse(
            record.getId(), record.getMemberInsulin().getId(), product.getName(), product.getActionType(),
            record.getInjectedAt(), record.getDoseUnit(), record.getMemo()
        );
    }
}
