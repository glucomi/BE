package com.example.ddadang.domain.record.weight.repository;

import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {

    List<WeightRecord> findByMemberIdOrderByMeasuredAtDesc(Long memberId);
}
