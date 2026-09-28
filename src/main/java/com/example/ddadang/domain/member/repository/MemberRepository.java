package com.example.ddadang.domain.member.repository;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
