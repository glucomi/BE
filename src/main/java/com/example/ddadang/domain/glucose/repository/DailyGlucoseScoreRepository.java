package com.example.ddadang.domain.glucose.repository;

import com.example.ddadang.domain.glucose.entity.DailyGlucoseScore;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyGlucoseScoreRepository extends JpaRepository<DailyGlucoseScore, Long> {

    Optional<DailyGlucoseScore> findByMemberIdAndScoreDate(Long memberId, LocalDate scoreDate);

    List<DailyGlucoseScore> findByMemberIdAndScoreDateBetween(Long memberId, LocalDate from, LocalDate to);

    boolean existsByMemberIdAndScoreDate(Long memberId, LocalDate scoreDate);
}
