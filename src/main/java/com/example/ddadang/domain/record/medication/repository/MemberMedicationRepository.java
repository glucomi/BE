package com.example.ddadang.domain.record.medication.repository;

import com.example.ddadang.domain.record.medication.entity.MemberMedication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMedicationRepository extends JpaRepository<MemberMedication, Long> {

    List<MemberMedication> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long memberId);
}
