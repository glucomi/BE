package com.example.ddadang.domain.cgm.repository;

import com.example.ddadang.domain.cgm.entity.CgmReading;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CgmReadingRepository extends JpaRepository<CgmReading, Long> {

    Page<CgmReading> findByIsensUserIdAndEventAtBetweenOrderByEventAtDesc(
        String isensUserId, OffsetDateTime start, OffsetDateTime end, Pageable pageable
    );

    List<CgmReading> findByIsensUserIdAndSerialNumberAndSeqNumberIn(
        String isensUserId, String serialNumber, List<Long> seqNumbers
    );
}
