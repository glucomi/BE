package com.example.ddadang.domain.record.exercise.repository;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findAllByOrderByNameAsc();

    List<Exercise> findByPopularTrueOrderByNameAsc();

    List<Exercise> findByNameContainingOrderByNameAsc(String keyword);

    List<Exercise> findByCodeIsNotNull();

    /**
     * 운동 DB에 없는(원본코드가 없는) 운동 중 운동 기록에서 쓰이지 않는 것.
     */
    @Query("""
        select e from Exercise e
        where e.code is null
          and not exists (select 1 from ExerciseRecord r where r.exercise = e)
        """)
    List<Exercise> findUnusedWithoutCode();
}
