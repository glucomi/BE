package com.example.ddadang.domain.record.insulin.controller;

import com.example.ddadang.domain.record.insulin.dto.request.InsulinRecordRequest;
import com.example.ddadang.domain.record.insulin.dto.response.InsulinRecordResponse;
import com.example.ddadang.domain.record.insulin.service.InsulinRecordService;
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

@Tag(name = "인슐린 기록", description = "인슐린 투여 기록 등록/조회/수정/삭제 (내 인슐린 중 하나를 골라 기록)")
@RestController
@RequiredArgsConstructor
public class InsulinRecordController {

    private final InsulinRecordService insulinRecordService;

    @Operation(
        summary = "인슐린 기록 등록",
        description = "삭제되지 않은 내 인슐린만 선택 가능(그 외 404 INSULIN_4041). 투여량은 0 초과, 소수 1자리."
    )
    @PostMapping("/api/insulin-records")
    public ResponseEntity<ApiResponse<InsulinRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid InsulinRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, insulinRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 인슐린 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/insulin-records")
    public ResponseEntity<ApiResponse<List<InsulinRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(insulinRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "인슐린 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/insulin-records/{insulinRecordId}")
    public ResponseEntity<ApiResponse<InsulinRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long insulinRecordId
    ) {
        return ApiResponse.success(insulinRecordService.get(memberId, insulinRecordId));
    }

    @Operation(
        summary = "인슐린 기록 수정",
        description = "내 인슐린/투여 일시/투여량/메모 전체를 교체한다. 내 인슐린을 바꿀 때는 삭제되지 않은 것만 가능. "
            + "본인 기록만 수정 가능(그 외 404)."
    )
    @PutMapping("/api/insulin-records/{insulinRecordId}")
    public ResponseEntity<ApiResponse<InsulinRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long insulinRecordId,
        @RequestBody @Valid InsulinRecordRequest request
    ) {
        return ApiResponse.success(insulinRecordService.update(memberId, insulinRecordId, request));
    }

    @Operation(summary = "인슐린 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/insulin-records/{insulinRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long insulinRecordId
    ) {
        insulinRecordService.delete(memberId, insulinRecordId);
        return ApiResponse.success(null);
    }
}
