package com.example.ddadang.domain.record.glucose.dto.response;

import com.example.ddadang.domain.record.glucose.entity.GlucoseRecord;
import java.time.LocalDateTime;

public record GlucoseRecordResponse(
    Long glucoseRecordId,
    LocalDateTime measuredAt,
    Integer valueMgDl,
    String memo
) {

    public static GlucoseRecordResponse from(GlucoseRecord record) {
        return new GlucoseRecordResponse(record.getId(), record.getMeasuredAt(), record.getValueMgDl(), record.getMemo());
    }
}
