package com.example.ddadang.domain.record.medication.controller;

import com.example.ddadang.domain.record.medication.dto.request.MedicationRecordRequest;
import com.example.ddadang.domain.record.medication.dto.response.MedicationRecordResponse;
import com.example.ddadang.domain.record.medication.service.MedicationRecordService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "복약 기록", description = "복약 기록 등록/조회/수정/삭제 (내 복용약 중 하나를 골라 기록)")
@RestController
@RequiredArgsConstructor
public class MedicationRecordController {

    private final MedicationRecordService medicationRecordService;

    @Operation(
        summary = "복약 기록 등록",
        description = "삭제되지 않은 내 복용약만 선택 가능(그 외 404 MEDICATION_4040)."
    )
    @PostMapping("/api/medication-records")
    public ResponseEntity<ApiResponse<MedicationRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid MedicationRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, medicationRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 복약 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/medication-records")
    public ResponseEntity<ApiResponse<List<MedicationRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(medicationRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "복약 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/medication-records/{medicationRecordId}")
    public ResponseEntity<ApiResponse<MedicationRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long medicationRecordId
    ) {
        return ApiResponse.success(medicationRecordService.get(memberId, medicationRecordId));
    }

    @Operation(
        summary = "복약 기록 수정",
        description = "내 복용약/복용 일시/메모 전체를 교체한다. 내 복용약을 바꿀 때는 삭제되지 않은 것만 가능. "
            + "본인 기록만 수정 가능(그 외 404)."
    )
    @PutMapping("/api/medication-records/{medicationRecordId}")
    public ResponseEntity<ApiResponse<MedicationRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long medicationRecordId,
        @RequestBody @Valid MedicationRecordRequest request
    ) {
        return ApiResponse.success(medicationRecordService.update(memberId, medicationRecordId, request));
    }

    @Operation(summary = "복약 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/medication-records/{medicationRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long medicationRecordId
    ) {
        medicationRecordService.delete(memberId, medicationRecordId);
        return ApiResponse.success(null);
    }
}
