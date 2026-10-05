package com.example.ddadang.domain.record.exercise.repository;

import com.example.ddadang.domain.record.exercise.entity.ExerciseRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    @EntityGraph(attributePaths = "exercise")
    Optional<ExerciseRecord> findWithExerciseById(Long id);

    @EntityGraph(attributePaths = "exercise")
    List<ExerciseRecord> findByMemberIdAndPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );
}
