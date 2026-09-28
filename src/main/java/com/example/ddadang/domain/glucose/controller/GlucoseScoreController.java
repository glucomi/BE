package com.example.ddadang.domain.glucose.controller;

import com.example.ddadang.domain.glucose.dto.response.DailyScoreResponse;
import com.example.ddadang.domain.glucose.service.DailyGlucoseScoreService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "혈당 점수", description = "달력용 날짜별 혈당 점수 (홈 주간 달력, COM-POP-10 월간 달력)")
@RestController
@RequiredArgsConstructor
public class GlucoseScoreController {

    private final DailyGlucoseScoreService dailyGlucoseScoreService;

    @Operation(
        summary = "날짜별 혈당 점수 조회",
        description = "from~to(포함, 최대 42일) 날짜별 점수를 조회한다. 확정된 날은 저장값(frozen=true), "
            + "오늘/확정 전인 날은 실시간 계산값. 점수가 없는 날은 score=null(status로 사유 구분)."
    )
    @GetMapping("/api/glucose/daily-scores")
    public ResponseEntity<ApiResponse<List<DailyScoreResponse>>> getDailyScores(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-05-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @Parameter(example = "2026-07-04") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(dailyGlucoseScoreService.getDailyScores(memberId, from, to));
    }
}
