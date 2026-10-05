package com.example.ddadang.domain.record.insulin.controller;

import com.example.ddadang.domain.record.insulin.dto.request.MemberInsulinCreateRequest;
import com.example.ddadang.domain.record.insulin.dto.request.MemberInsulinUpdateRequest;
import com.example.ddadang.domain.record.insulin.dto.response.InsulinProductResponse;
import com.example.ddadang.domain.record.insulin.dto.response.MemberInsulinResponse;
import com.example.ddadang.domain.record.insulin.service.InsulinService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인슐린", description = "인슐린 제품 목록, 내 인슐린 등록/수정/삭제")
@RestController
@RequiredArgsConstructor
public class InsulinController {

    private final InsulinService insulinService;

    @Operation(summary = "인슐린 제품 목록", description = "작용 유형 → 제품명 순. keyword가 있으면 제품명으로 검색한다.")
    @GetMapping("/api/insulin-products")
    public ResponseEntity<ApiResponse<List<InsulinProductResponse>>> getProducts(
        @Parameter(description = "제품명 검색어(선택)", example = "노보") @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(insulinService.getProducts(keyword));
    }

    @Operation(summary = "내 인슐린 목록", description = "등록한 순. 삭제한 인슐린은 제외.")
    @GetMapping("/api/members/me/insulins")
    public ResponseEntity<ApiResponse<List<MemberInsulinResponse>>> getMyInsulins(
        @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(insulinService.getMyInsulins(memberId));
    }

    @Operation(
        summary = "내 인슐린 등록",
        description = "제품과 기본 투여량(U)을 등록한다. 이미 등록한 제품이면 409 INSULIN_4090, 없는 제품이면 404 INSULIN_4040."
    )
    @PostMapping("/api/members/me/insulins")
    public ResponseEntity<ApiResponse<MemberInsulinResponse>> addMyInsulin(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid MemberInsulinCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, insulinService.addMyInsulin(memberId, request));
    }

    @Operation(summary = "내 인슐린 기본 투여량 수정", description = "본인 인슐린만 수정 가능(그 외 404 INSULIN_4041).")
    @PutMapping("/api/members/me/insulins/{memberInsulinId}")
    public ResponseEntity<ApiResponse<MemberInsulinResponse>> updateMyInsulin(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memberInsulinId,
        @RequestBody @Valid MemberInsulinUpdateRequest request
    ) {
        return ApiResponse.success(insulinService.updateMyInsulin(memberId, memberInsulinId, request));
    }

    @Operation(
        summary = "내 인슐린 삭제",
        description = "목록과 기록하기 선택지에서 빠지지만, 이미 남긴 인슐린 기록은 그대로 조회된다."
    )
    @DeleteMapping("/api/members/me/insulins/{memberInsulinId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyInsulin(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long memberInsulinId
    ) {
        insulinService.deleteMyInsulin(memberId, memberInsulinId);
        return ApiResponse.success(null);
    }
}
