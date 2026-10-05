package com.example.ddadang.domain.record.exercise.controller;

import com.example.ddadang.domain.record.exercise.dto.response.ExerciseResponse;
import com.example.ddadang.domain.record.exercise.service.ExerciseService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "운동", description = "운동 목록 (MO-EXERCISE-020 운동 추가)")
@RestController
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(
        summary = "운동 목록",
        description = "운동명(가나다) 순. keyword가 있으면 운동명 부분 일치 검색(최대 20자), 없고 popular=true면 인기 운동만. "
            + "kcalPer30Min은 회원의 최근 체중 기준 30분 소모 열량(체중 기록이 없으면 null)."
    )
    @GetMapping("/api/exercises")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercises(
        @AuthenticationPrincipal Long memberId,
        @Parameter(description = "운동명 검색어(선택, 최대 20자)", example = "걷기")
        @RequestParam(required = false) @Size(max = 20) String keyword,
        @Parameter(description = "인기 운동만 조회") @RequestParam(defaultValue = "false") boolean popular
    ) {
        return ApiResponse.success(exerciseService.getExercises(memberId, keyword, popular));
    }

    @Operation(
        summary = "최근 기록한 운동",
        description = "가장 최근에 기록한 순, 같은 운동은 1번만, 최대 4개. 기록이 없으면 빈 배열(섹션 미노출)."
    )
    @GetMapping("/api/exercises/recent")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getRecentExercises(
        @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(exerciseService.getRecentExercises(memberId));
    }
}
