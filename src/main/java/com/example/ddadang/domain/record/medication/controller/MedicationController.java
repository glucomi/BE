package com.example.ddadang.domain.record.medication.controller;

import com.example.ddadang.domain.record.medication.dto.request.MemberMedicationRequest;
import com.example.ddadang.domain.record.medication.dto.response.MemberMedicationResponse;
import com.example.ddadang.domain.record.medication.service.MedicationService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "복용약", description = "내 복용약 등록/삭제 (MO-DRUG-020, 030)")
@RestController
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @Operation(summary = "내 복용약 목록", description = "등록한 순. 삭제한 약은 제외.")
    @GetMapping("/api/members/me/medications")
    public ResponseEntity<ApiResponse<List<MemberMedicationResponse>>> getMyMedications(
        @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(medicationService.getMyMedications(memberId));
    }

    @Operation(summary = "내 복용약 등록", description = "약 종류는 필수, 제품명은 선택(공백이면 null).")
    @PostMapping("/api/members/me/medications")
    public ResponseEntity<ApiResponse<MemberMedicationResponse>> addMyMedication(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid MemberMedicationRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, medicationService.addMyMedication(memberId, request));
    }

    @Operation(
        summary = "내 복용약 삭제",
        description = "목록과 기록하기 선택지에서 빠지지만, 이미 남긴 복약 기록은 그대로 조회된다."
    )
    @DeleteMapping("/api/members/me/medications/{memberMedicationId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyMedication(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memberMedicationId
    ) {
        medicationService.deleteMyMedication(memberId, memberMedicationId);
        return ApiResponse.success(null);
    }
}
