package com.example.ddadang.domain.record.memo.dto.response;

import com.example.ddadang.domain.record.memo.entity.MemoRecord;
import java.time.LocalDateTime;

public record MemoRecordResponse(
    Long memoRecordId,
    LocalDateTime recordedAt,
    String content
) {

    public static MemoRecordResponse from(MemoRecord record) {
        return new MemoRecordResponse(record.getId(), record.getRecordedAt(), record.getContent());
    }
}
