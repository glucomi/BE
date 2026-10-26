package com.example.ddadang.domain.record.memo.controller;

import com.example.ddadang.domain.record.memo.dto.request.MemoRecordRequest;
import com.example.ddadang.domain.record.memo.dto.response.MemoRecordResponse;
import com.example.ddadang.domain.record.memo.service.MemoRecordService;
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

@Tag(name = "메모 기록", description = "메모 기록 등록/조회/수정/삭제 (사진 첨부는 추후 지원)")
@RestController
@RequiredArgsConstructor
public class MemoRecordController {

    private final MemoRecordService memoRecordService;

    @Operation(summary = "메모 기록 등록", description = "내용은 필수, 최대 1000자. 사진 첨부는 추후 지원.")
    @PostMapping("/api/memo-records")
    public ResponseEntity<ApiResponse<MemoRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid MemoRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, memoRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 메모 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/memo-records")
    public ResponseEntity<ApiResponse<List<MemoRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(memoRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "메모 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/memo-records/{memoRecordId}")
    public ResponseEntity<ApiResponse<MemoRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memoRecordId
    ) {
        return ApiResponse.success(memoRecordService.get(memberId, memoRecordId));
    }

    @Operation(summary = "메모 기록 수정", description = "기록 일시/내용 전체를 교체한다. 본인 기록만 수정 가능(그 외 404).")
    @PutMapping("/api/memo-records/{memoRecordId}")
    public ResponseEntity<ApiResponse<MemoRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memoRecordId,
        @RequestBody @Valid MemoRecordRequest request
    ) {
        return ApiResponse.success(memoRecordService.update(memberId, memoRecordId, request));
    }

    @Operation(summary = "메모 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/memo-records/{memoRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memoRecordId
    ) {
        memoRecordService.delete(memberId, memoRecordId);
        return ApiResponse.success(null);
    }
}
