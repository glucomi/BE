package com.example.ddadang.domain.record.medication.dto.request;

import com.example.ddadang.domain.record.medication.enums.MedicationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberMedicationRequest(
    @Schema(description = "DIABETES 당뇨약 / HYPERLIPIDEMIA 고지혈증약 / HYPERTENSION 고혈압약 / SUPPLEMENT 영양제 / ETC 기타")
    @NotNull MedicationCategory category,
    @Schema(description = "제품명(선택, 최대 100자)", example = "다이아벡스정") @Size(max = 100) String productName
) {
}
