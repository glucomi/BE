package com.example.ddadang.domain.record.exercise.repository;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findAllByOrderByNameAsc();

    List<Exercise> findByPopularTrueOrderByNameAsc();

    List<Exercise> findByNameContainingOrderByNameAsc(String keyword);
}
