package com.example.ddadang.domain.record.insulin.repository;

import com.example.ddadang.domain.record.insulin.entity.MemberInsulin;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInsulinRepository extends JpaRepository<MemberInsulin, Long> {

    @EntityGraph(attributePaths = "insulinProduct")
    List<MemberInsulin> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long memberId);

    @EntityGraph(attributePaths = "insulinProduct")
    Optional<MemberInsulin> findWithProductById(Long id);

    boolean existsByMemberIdAndInsulinProductIdAndDeletedAtIsNull(Long memberId, Long insulinProductId);
}
