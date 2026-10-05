package com.example.ddadang.domain.record.glucose.repository;

import com.example.ddadang.domain.record.glucose.entity.GlucoseRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlucoseRecordRepository extends JpaRepository<GlucoseRecord, Long> {

    List<GlucoseRecord> findByMemberIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );
}
