package com.example.ddadang.domain.member.repository;

import com.example.ddadang.domain.member.entity.MemberWithdrawal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {

    Optional<MemberWithdrawal> findByMemberId(Long memberId);
}
