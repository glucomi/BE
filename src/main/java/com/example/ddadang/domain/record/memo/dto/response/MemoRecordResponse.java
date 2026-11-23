package com.example.ddadang.domain.record.memo.dto.response;

import com.example.ddadang.domain.photo.dto.PhotoResponse;
import com.example.ddadang.domain.record.memo.entity.MemoRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record MemoRecordResponse(
    Long memoRecordId,
    LocalDateTime recordedAt,
    String content,
    @Schema(description = "첨부 사진(첨부 순서)") List<PhotoResponse> photos
) {

    public static MemoRecordResponse from(MemoRecord record, List<PhotoResponse> photos) {
        return new MemoRecordResponse(record.getId(), record.getRecordedAt(), record.getContent(), photos);
    }
}
