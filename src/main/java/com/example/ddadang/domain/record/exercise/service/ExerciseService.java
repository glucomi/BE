package com.example.ddadang.domain.record.exercise.service;

import com.example.ddadang.domain.record.exercise.dto.response.ExerciseResponse;
import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import com.example.ddadang.domain.record.exercise.status.ExerciseErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    /**
     * 운동명 순. keyword가 있으면 운동명 검색, 없고 popularOnly면 인기 운동만.
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(String keyword, boolean popularOnly) {
        List<Exercise> exercises;
        if (keyword != null && !keyword.isBlank()) {
            exercises = exerciseRepository.findByNameContainingOrderByNameAsc(keyword.strip());
        } else if (popularOnly) {
            exercises = exerciseRepository.findByPopularTrueOrderByNameAsc();
        } else {
            exercises = exerciseRepository.findAllByOrderByNameAsc();
        }
        return exercises.stream().map(ExerciseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Exercise getExercise(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new GeneralException(ExerciseErrorStatus.EXERCISE_NOT_FOUND));
    }
}
