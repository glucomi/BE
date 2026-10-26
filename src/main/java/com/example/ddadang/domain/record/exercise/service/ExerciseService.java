package com.example.ddadang.domain.record.exercise.service;

import com.example.ddadang.domain.record.exercise.dto.response.ExerciseResponse;
import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRecordRepository;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import com.example.ddadang.domain.record.exercise.status.ExerciseErrorStatus;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.global.exception.GeneralException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    /** MO-EXERCISE-020 "최근 기록한 운동" 최대 노출 개수 */
    private static final int RECENT_LIMIT = 4;

    private final ExerciseRepository exerciseRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final WeightRecordRepository weightRecordRepository;

    /**
     * 운동명 순. keyword가 있으면 운동명 검색, 없고 popularOnly면 인기 운동만.
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(Long memberId, String keyword, boolean popularOnly) {
        List<Exercise> exercises;
        if (keyword != null && !keyword.isBlank()) {
            exercises = exerciseRepository.findByNameContainingOrderByNameAsc(keyword.strip());
        } else if (popularOnly) {
            exercises = exerciseRepository.findByPopularTrueOrderByNameAsc();
        } else {
            exercises = exerciseRepository.findAllByOrderByNameAsc();
        }
        return toResponses(memberId, exercises);
    }

    /**
     * 최근 기록한 운동: 가장 최근에 한 순, 같은 운동은 1번만, 최대 4개. 기록을 삭제하면 함께 빠진다.
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getRecentExercises(Long memberId) {
        List<Long> ids = exerciseRecordRepository.findRecentExerciseIds(memberId, PageRequest.of(0, RECENT_LIMIT));
        Map<Long, Exercise> byId = exerciseRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(Exercise::getId, Function.identity()));
        return toResponses(memberId, ids.stream().map(byId::get).filter(Objects::nonNull).toList());
    }

    @Transactional(readOnly = true)
    public Exercise getExercise(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new GeneralException(ExerciseErrorStatus.EXERCISE_NOT_FOUND));
    }

    /**
     * 목록의 "kcal / 30분"은 회원의 가장 최근 체중 기준이다.
     */
    private List<ExerciseResponse> toResponses(Long memberId, List<Exercise> exercises) {
        BigDecimal weightKg = weightRecordRepository.findFirstByMemberIdOrderByMeasuredAtDesc(memberId)
            .map(WeightRecord::getWeightKg)
            .orElse(null);
        return exercises.stream().map(exercise -> ExerciseResponse.of(exercise, weightKg)).toList();
    }
}
