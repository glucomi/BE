package com.example.ddadang.domain.cgm.repository;

import com.example.ddadang.domain.cgm.entity.CgmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CgmTokenRepository extends JpaRepository<CgmToken, Long> {

    Optional<CgmToken> findByIsensUserId(String isensUserId);
}
