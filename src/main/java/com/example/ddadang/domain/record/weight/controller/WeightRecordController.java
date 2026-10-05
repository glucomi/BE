package com.example.ddadang.domain.record.weight.controller;

import com.example.ddadang.domain.record.weight.dto.request.WeightRecordRequest;
import com.example.ddadang.domain.record.weight.dto.response.WeightRecordResponse;
import com.example.ddadang.domain.record.weight.service.WeightRecordService;
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

@Tag(name = "체중 기록", description = "체중 기록 등록/조회/수정/삭제")
@RestController
@RequiredArgsConstructor
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    @Operation(summary = "체중 기록 등록", description = "체중은 20~300 kg, 소수 1자리.")
    @PostMapping("/api/weight-records")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid WeightRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, weightRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 체중 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/weight-records")
    public ResponseEntity<ApiResponse<List<WeightRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(weightRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "체중 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/weight-records/{weightRecordId}")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long weightRecordId
    ) {
        return ApiResponse.success(weightRecordService.get(memberId, weightRecordId));
    }

    @Operation(summary = "체중 기록 수정", description = "측정 일시/체중 전체를 교체한다. 본인 기록만 수정 가능(그 외 404).")
    @PutMapping("/api/weight-records/{weightRecordId}")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long weightRecordId,
        @RequestBody @Valid WeightRecordRequest request
    ) {
        return ApiResponse.success(weightRecordService.update(memberId, weightRecordId, request));
    }

    @Operation(summary = "체중 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/weight-records/{weightRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long weightRecordId
    ) {
        weightRecordService.delete(memberId, weightRecordId);
        return ApiResponse.success(null);
    }
}
