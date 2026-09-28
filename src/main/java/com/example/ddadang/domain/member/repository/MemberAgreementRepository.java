package com.example.ddadang.domain.member.repository;

import com.example.ddadang.domain.member.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long> {
}
