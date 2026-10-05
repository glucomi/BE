package com.example.ddadang.domain.record.medication.dto.response;

import com.example.ddadang.domain.record.medication.entity.MedicationRecord;
import com.example.ddadang.domain.record.medication.entity.MemberMedication;
import com.example.ddadang.domain.record.medication.enums.MedicationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MedicationRecordResponse(
    Long medicationRecordId,
    Long memberMedicationId,
    MedicationCategory category,
    @Schema(description = "기록에 저장된 제품명") String productName,
    LocalDateTime takenAt,
    String memo
) {

    public static MedicationRecordResponse from(MedicationRecord record) {
        MemberMedication medication = record.getMemberMedication();
        return new MedicationRecordResponse(
            record.getId(), medication.getId(), medication.getCategory(), record.getProductName(),
            record.getTakenAt(), record.getMemo()
        );
    }
}
