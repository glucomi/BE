package com.example.ddadang.domain.record.weight.repository;

import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {

    List<WeightRecord> findByMemberIdOrderByMeasuredAtDesc(Long memberId);

    List<WeightRecord> findByMemberIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtDesc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );

    Optional<WeightRecord> findFirstByMemberIdOrderByMeasuredAtDesc(Long memberId);

    /**
     * 기준 시각 이전(포함) 가장 최근 체중. 운동 소모 열량 계산 등에 쓴다.
     */
    Optional<WeightRecord> findFirstByMemberIdAndMeasuredAtLessThanEqualOrderByMeasuredAtDesc(
        Long memberId, LocalDateTime at
    );

    Optional<WeightRecord> findFirstByMemberIdOrderByMeasuredAtAsc(Long memberId);
}
