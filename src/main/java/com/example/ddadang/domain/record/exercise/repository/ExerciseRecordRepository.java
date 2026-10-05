package com.example.ddadang.domain.record.exercise.repository;

import com.example.ddadang.domain.record.exercise.entity.ExerciseRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    @EntityGraph(attributePaths = "exercise")
    Optional<ExerciseRecord> findWithExerciseById(Long id);

    @EntityGraph(attributePaths = "exercise")
    List<ExerciseRecord> findByMemberIdAndPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );

    /**
     * 회원이 기록한 운동 ID를 가장 최근에 한 순으로 중복 없이 조회한다.
     */
    @Query("""
        select r.exercise.id from ExerciseRecord r
        where r.member.id = :memberId
        group by r.exercise.id
        order by max(r.performedAt) desc
        """)
    List<Long> findRecentExerciseIds(@Param("memberId") Long memberId, Pageable pageable);
}
