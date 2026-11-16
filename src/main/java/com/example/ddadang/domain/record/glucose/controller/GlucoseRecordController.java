package com.example.ddadang.domain.record.glucose.controller;

import com.example.ddadang.domain.record.glucose.dto.request.GlucoseRecordRequest;
import com.example.ddadang.domain.record.glucose.dto.response.GlucoseRecordResponse;
import com.example.ddadang.domain.record.glucose.service.GlucoseRecordService;
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

@Tag(name = "혈당 기록", description = "혈당계(BGM)로 잰 혈당 수기 기록 등록/조회/수정/삭제")
@RestController
@RequiredArgsConstructor
public class GlucoseRecordController {

    private final GlucoseRecordService glucoseRecordService;

    @Operation(summary = "혈당 기록 등록", description = "혈당값은 10~600 mg/dL. CGM 측정값과는 별도로 저장된다.")
    @PostMapping("/api/glucose-records")
    public ResponseEntity<ApiResponse<GlucoseRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid GlucoseRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, glucoseRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 혈당 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/glucose-records")
    public ResponseEntity<ApiResponse<List<GlucoseRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(glucoseRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "혈당 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/glucose-records/{glucoseRecordId}")
    public ResponseEntity<ApiResponse<GlucoseRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long glucoseRecordId
    ) {
        return ApiResponse.success(glucoseRecordService.get(memberId, glucoseRecordId));
    }

    @Operation(summary = "혈당 기록 수정", description = "측정 일시/혈당값/메모 전체를 교체한다. 본인 기록만 수정 가능(그 외 404).")
    @PutMapping("/api/glucose-records/{glucoseRecordId}")
    public ResponseEntity<ApiResponse<GlucoseRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long glucoseRecordId,
        @RequestBody @Valid GlucoseRecordRequest request
    ) {
        return ApiResponse.success(glucoseRecordService.update(memberId, glucoseRecordId, request));
    }

    @Operation(summary = "혈당 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/glucose-records/{glucoseRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long glucoseRecordId
    ) {
        glucoseRecordService.delete(memberId, glucoseRecordId);
        return ApiResponse.success(null);
    }
}
