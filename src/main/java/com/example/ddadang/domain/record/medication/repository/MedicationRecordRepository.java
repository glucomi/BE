package com.example.ddadang.domain.record.medication.repository;

import com.example.ddadang.domain.record.medication.entity.MedicationRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRecordRepository extends JpaRepository<MedicationRecord, Long> {

    @EntityGraph(attributePaths = "memberMedication")
    Optional<MedicationRecord> findWithMedicationById(Long id);

    @EntityGraph(attributePaths = "memberMedication")
    List<MedicationRecord> findByMemberIdAndTakenAtGreaterThanEqualAndTakenAtLessThanOrderByTakenAtAsc(
        Long memberId, LocalDateTime from, LocalDateTime to
    );
}
