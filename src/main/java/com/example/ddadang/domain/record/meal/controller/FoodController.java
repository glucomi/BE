package com.example.ddadang.domain.record.meal.controller;

import com.example.ddadang.domain.record.meal.dto.request.CustomFoodCreateRequest;
import com.example.ddadang.domain.record.meal.dto.response.FoodDetailResponse;
import com.example.ddadang.domain.record.meal.dto.response.FoodSummaryResponse;
import com.example.ddadang.domain.record.meal.service.FoodService;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "음식", description = "음식 검색/상세, 즐겨찾기, 직접 등록 (MO-MEAL-010, 020, 030, 050)")
@RestController
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @Operation(
        summary = "음식 검색",
        description = "음식 DB와 내가 직접 등록한 음식에서 음식명/브랜드명으로 검색한다. 이름이 짧은(더 정확한) 순으로 정렬. "
            + "page/size만 사용하고 sort는 비워둘 것."
    )
    @GetMapping("/api/foods")
    public ResponseEntity<ApiResponse<Page<FoodSummaryResponse>>> search(
        @AuthenticationPrincipal Long memberId,
        @Parameter(description = "검색어", example = "밥") @RequestParam @NotBlank String keyword,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(foodService.search(memberId, keyword, pageable));
    }

    @Operation(
        summary = "음식 상세 조회",
        description = "영양 성분(기준 섭취량 기준)과 즐겨찾기 여부를 조회한다. 섭취량 변경 시 환산은 FE에서 비율로 계산한다."
    )
    @GetMapping("/api/foods/{foodId}")
    public ResponseEntity<ApiResponse<FoodDetailResponse>> getDetail(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long foodId
    ) {
        return ApiResponse.success(foodService.getDetail(memberId, foodId));
    }

    @Operation(summary = "즐겨찾기 음식 목록", description = "최근에 즐겨찾기한 순.")
    @GetMapping("/api/foods/favorites")
    public ResponseEntity<ApiResponse<Page<FoodSummaryResponse>>> getFavorites(
        @AuthenticationPrincipal Long memberId,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(foodService.getFavorites(memberId, pageable));
    }

    @Operation(summary = "즐겨찾기 추가", description = "이미 즐겨찾기한 음식이면 그대로 성공 처리한다.")
    @PostMapping("/api/foods/{foodId}/favorite")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long foodId
    ) {
        foodService.addFavorite(memberId, foodId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "즐겨찾기 해제", description = "즐겨찾기하지 않은 음식이어도 성공 처리한다.")
    @DeleteMapping("/api/foods/{foodId}/favorite")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long foodId
    ) {
        foodService.removeFavorite(memberId, foodId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "직접 등록한 음식 목록", description = "최근 등록 순.")
    @GetMapping("/api/foods/custom")
    public ResponseEntity<ApiResponse<Page<FoodSummaryResponse>>> getCustomFoods(
        @AuthenticationPrincipal Long memberId,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(foodService.getCustomFoods(memberId, pageable));
    }

    @Operation(
        summary = "음식 직접 등록",
        description = "MO-MEAL-050. 음식명, 기준 섭취량/단위, 열량은 필수이고 나머지 영양 성분은 선택. 등록한 회원에게만 보인다."
    )
    @PostMapping("/api/foods/custom")
    public ResponseEntity<ApiResponse<FoodDetailResponse>> createCustomFood(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid CustomFoodCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, foodService.createCustomFood(memberId, request));
    }
}
