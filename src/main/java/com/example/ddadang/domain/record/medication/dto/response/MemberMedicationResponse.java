package com.example.ddadang.domain.record.medication.dto.response;

import com.example.ddadang.domain.record.medication.entity.MemberMedication;
import com.example.ddadang.domain.record.medication.enums.MedicationCategory;

public record MemberMedicationResponse(
    Long memberMedicationId,
    MedicationCategory category,
    String productName
) {

    public static MemberMedicationResponse from(MemberMedication medication) {
        return new MemberMedicationResponse(medication.getId(), medication.getCategory(), medication.getProductName());
    }
}
