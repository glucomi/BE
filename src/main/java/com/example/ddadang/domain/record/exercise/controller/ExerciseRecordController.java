package com.example.ddadang.domain.record.exercise.controller;

import com.example.ddadang.domain.record.exercise.dto.request.ExerciseRecordRequest;
import com.example.ddadang.domain.record.exercise.dto.response.ExerciseRecordResponse;
import com.example.ddadang.domain.record.exercise.service.ExerciseRecordService;
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

@Tag(name = "운동 기록", description = "운동 기록 등록/조회/수정/삭제 (소모 열량 자동 계산)")
@RestController
@RequiredArgsConstructor
public class ExerciseRecordController {

    private final ExerciseRecordService exerciseRecordService;

    @Operation(
        summary = "운동 기록 등록",
        description = "소모 열량 = MET × 체중(kg) × 시간(h). 체중은 운동 일시 이전 가장 최근 체중 기록을 쓰고, "
            + "이전 기록이 없으면 가장 오래된 체중 기록을 쓴다. 체중 기록이 하나도 없으면 kcal=null. "
            + "없는 운동이면 404 EXERCISE_4040."
    )
    @PostMapping("/api/exercise-records")
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> create(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid ExerciseRecordRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, exerciseRecordService.create(memberId, request));
    }

    @Operation(summary = "날짜별 운동 기록 목록", description = "해당 날짜 00:00~24:00(KST) 기록을 시간순으로 조회한다.")
    @GetMapping("/api/exercise-records")
    public ResponseEntity<ApiResponse<List<ExerciseRecordResponse>>> getByDate(
        @AuthenticationPrincipal Long memberId,
        @Parameter(example = "2026-10-05") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(exerciseRecordService.getByDate(memberId, date));
    }

    @Operation(summary = "운동 기록 상세 조회", description = "본인 기록만 조회 가능(그 외 404).")
    @GetMapping("/api/exercise-records/{exerciseRecordId}")
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> get(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long exerciseRecordId
    ) {
        return ApiResponse.success(exerciseRecordService.get(memberId, exerciseRecordId));
    }

    @Operation(summary = "운동 기록 수정", description = "운동/일시/시간/메모 전체를 교체하고 소모 열량을 다시 계산한다. 본인 기록만 수정 가능(그 외 404).")
    @PutMapping("/api/exercise-records/{exerciseRecordId}")
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> update(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long exerciseRecordId,
        @RequestBody @Valid ExerciseRecordRequest request
    ) {
        return ApiResponse.success(exerciseRecordService.update(memberId, exerciseRecordId, request));
    }

    @Operation(summary = "운동 기록 삭제", description = "본인 기록만 삭제 가능(그 외 404).")
    @DeleteMapping("/api/exercise-records/{exerciseRecordId}")
    public ResponseEntity<ApiResponse<Void>> delete(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long exerciseRecordId
    ) {
        exerciseRecordService.delete(memberId, exerciseRecordId);
        return ApiResponse.success(null);
    }
}
