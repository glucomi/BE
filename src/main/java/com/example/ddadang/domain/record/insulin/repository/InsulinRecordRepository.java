package com.example.ddadang.domain.record.insulin.repository;

import com.example.ddadang.domain.record.insulin.entity.InsulinRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsulinRecordRepository extends JpaRepository<InsulinRecord, Long> {

    @EntityGraph(attributePaths = {"memberInsulin", "memberInsulin.insulinProduct"})
    Optional<InsulinRecord> findWithInsulinById(Long id);

    @EntityGraph(attributePaths = {"memberInsulin", "memberInsulin.insulinProduct"})
    List<InsulinRecord> findByMemberIdAndInjectedAtGreaterThanEqualAndInjectedAtLessThanOrderByInjectedAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );
}
