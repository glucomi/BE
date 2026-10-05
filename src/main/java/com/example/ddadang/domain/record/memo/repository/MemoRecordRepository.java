package com.example.ddadang.domain.record.memo.repository;

import com.example.ddadang.domain.record.memo.entity.MemoRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRecordRepository extends JpaRepository<MemoRecord, Long> {

    List<MemoRecord> findByMemberIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );
}
