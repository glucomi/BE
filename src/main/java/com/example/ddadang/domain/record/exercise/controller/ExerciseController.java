package com.example.ddadang.domain.record.exercise.controller;

import com.example.ddadang.domain.record.exercise.dto.response.ExerciseResponse;
import com.example.ddadang.domain.record.exercise.service.ExerciseService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "운동", description = "운동 목록 (운동 기록하기에서 선택)")
@RestController
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(
        summary = "운동 목록",
        description = "운동명 순. keyword가 있으면 운동명 검색, 없고 popular=true면 인기 운동만 조회한다."
    )
    @GetMapping("/api/exercises")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercises(
        @Parameter(description = "운동명 검색어(선택)", example = "걷기") @RequestParam(required = false) String keyword,
        @Parameter(description = "인기 운동만 조회") @RequestParam(defaultValue = "false") boolean popular
    ) {
        return ApiResponse.success(exerciseService.getExercises(keyword, popular));
    }
}
