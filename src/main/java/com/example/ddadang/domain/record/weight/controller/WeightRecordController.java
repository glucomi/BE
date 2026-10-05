package com.example.ddadang.domain.record.weight.controller;

import com.example.ddadang.domain.record.weight.dto.request.WeightRecordRequest;
import com.example.ddadang.domain.record.weight.dto.response.WeightRecordResponse;
import com.example.ddadang.domain.record.weight.service.WeightRecordService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "체중 기록", description = "체중 기록 (MO-WEIGHT-010). 날짜 단위로 하루 1건")
@RestController
@RequiredArgsConstructor
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    @Operation(
        summary = "체중 저장(등록/수정)",
        description = "해당 날짜에 기록이 있으면 체중을 갱신하고, 없으면 새로 저장한다. 체중은 0 초과 200 이하, 0.1kg 단위."
    )
    @PutMapping("/api/weight-records/{date}")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> save(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestBody @Valid WeightRecordRequest request
    ) {
        return ApiResponse.success(weightRecordService.save(memberId, date, request));
    }

    @Operation(
        summary = "최근 체중",
        description = "가장 최근 체중 기록. 체중 입력 화면 기본값(직전 저장값)으로 쓴다. 기록이 없으면 result=null."
    )
    @GetMapping("/api/weight-records/latest")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> getLatest(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(weightRecordService.getLatest(memberId));
    }

    @Operation(summary = "날짜별 체중 조회", description = "해당 날짜 기록. 없으면 result=null (기본 화면으로 진입).")
    @GetMapping("/api/weight-records/{date}")
    public ResponseEntity<ApiResponse<WeightRecordResponse>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(weightRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "체중 기록 삭제", description = "해당 날짜 기록을 삭제한다. 기록이 없으면 404 WEIGHT_RECORD_4040.")
    @DeleteMapping("/api/weight-records/{date}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        weightRecordService.delete(memberId, date);
        return ApiResponse.success(null);
    }
}
