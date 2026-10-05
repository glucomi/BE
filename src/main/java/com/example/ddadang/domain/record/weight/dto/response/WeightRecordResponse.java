package com.example.ddadang.domain.record.weight.dto.response;

import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeightRecordResponse(
    Long weightRecordId,
    LocalDateTime measuredAt,
    BigDecimal weightKg
) {

    public static WeightRecordResponse from(WeightRecord record) {
        return new WeightRecordResponse(record.getId(), record.getMeasuredAt(), record.getWeightKg());
    }
}
