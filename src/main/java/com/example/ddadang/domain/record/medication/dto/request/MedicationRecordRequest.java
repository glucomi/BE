package com.example.ddadang.domain.record.medication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MedicationRecordRequest(
    @Schema(description = "내 복용약 ID") @NotNull Long memberMedicationId,
    @Schema(description = "복용 일시", example = "2026-10-05T08:30:00") @NotNull LocalDateTime takenAt,
    @Schema(
        description = "제품명(선택, 최대 20자). 선택한 내 복용약 제품명을 기본값으로 채워 보내고, 기록 화면에서 수정 가능",
        example = "다이아벡스정"
    )
    @Size(max = 20) String productName,
    @Schema(description = "메모(선택, 최대 1000자)") @Size(max = 1000) String memo
) {
}
