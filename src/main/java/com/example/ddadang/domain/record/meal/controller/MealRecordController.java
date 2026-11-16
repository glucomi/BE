package com.example.ddadang.domain.record.meal.controller;

import com.example.ddadang.domain.record.meal.dto.request.MealRecordRequest;
import com.example.ddadang.domain.record.meal.dto.response.MealRecordResponse;
import com.example.ddadang.domain.record.meal.service.MealRecordService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "식사 기록", description = "식사 기록 저장/조회/수정/삭제 (MO-MEAL-020 기록하기)")
@RestController
@RequiredArgsConstructor
public class MealRecordController {

    private final MealRecordService mealRecordService;

    @Operation(
        summary = "식사 기록 저장",
        description = "음식 검색에서 담은 음식들을 한 번에 저장한다. 영양 성분은 서버에서 섭취량 기준으로 환산해 저장하며, "
            + "unit은 음식의 기준 단위(G/ML) 또는 SERVING(인분)만 가능(그 외 400 MEAL_4000). 사진 첨부는 추후 지원."
    )
    @PostMapping("/api/meal-records")
    public ResponseEntity<ApiResponse<MealRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid MealRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, mealRecordService.create(memberId, request));
    }

    @Operation(summary = "식사 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/meal-records/{mealRecordId}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long mealRecordId
    ) {
        return ApiResponse.success(mealRecordService.get(memberId, mealRecordId));
    }

    @Operation(
        summary = "식사 기록 수정",
        description = "저장 요청과 같은 형식으로 식사 일시/메모/메뉴 전체를 교체한다. "
            + "영양 성분은 수정 시점의 음식 정보로 다시 환산한다. 본인 기록만 수정 가능(그 외 404)."
    )
    @PutMapping("/api/meal-records/{mealRecordId}")
    public ResponseEntity<ApiResponse<MealRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long mealRecordId,
        @RequestBody @Valid MealRecordRequest request
    ) {
        return ApiResponse.success(mealRecordService.update(memberId, mealRecordId, request));
    }

    @Operation(summary = "식사 기록 삭제", description = "메뉴도 함께 삭제한다. 본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/meal-records/{mealRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long mealRecordId
    ) {
        mealRecordService.delete(memberId, mealRecordId);
        return ApiResponse.success(null);
    }
}
