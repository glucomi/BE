package com.example.ddadang.domain.home.controller;

import com.example.ddadang.domain.home.dto.HomeResponse;
import com.example.ddadang.domain.home.service.HomeService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈", description = "홈 화면 혈당 그래프 및 요약 지표 (MO-HOME-010)")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(
        summary = "홈 화면 조회",
        description = "선택한 날짜(KST 00:00~24:00)의 센서 상태, 목표 혈당 범위, CGM 그래프 포인트, 요약 지표, 식사 표시를 조회한다. "
            + "CGM 데이터는 /api/cgm/sync로 적재된 자체 DB 기준. 혈당 점수/스파이크 횟수는 계산 기준 확정 전까지 null."
    )
    @GetMapping("/api/home")
    public ResponseEntity<ApiResponse<HomeResponse>> getHome(
        @AuthenticationPrincipal Long memberId,
        @Parameter(description = "조회 날짜(yyyy-MM-dd). 비우면 오늘(KST)", example = "2026-09-28")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate target = date != null ? date : LocalDate.now(HomeService.KST);
        return ApiResponse.success(homeService.getHome(memberId, target));
    }
}
