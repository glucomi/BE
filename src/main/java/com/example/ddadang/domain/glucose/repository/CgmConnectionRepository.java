package com.example.ddadang.domain.glucose.repository;

import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.enums.CgmConnectionStatus;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CgmConnectionRepository extends JpaRepository<CgmConnection, Long> {

    Optional<CgmConnection> findByProviderAndExternalUserId(CgmProvider provider, String externalUserId);

    Optional<CgmConnection> findFirstByMemberIdAndProviderAndStatusOrderByUpdatedAtDesc(
        Long memberId, CgmProvider provider, CgmConnectionStatus status
    );
}
