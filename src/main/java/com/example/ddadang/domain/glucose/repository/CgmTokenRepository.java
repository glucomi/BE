package com.example.ddadang.domain.glucose.repository;

import com.example.ddadang.domain.glucose.entity.CgmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CgmTokenRepository extends JpaRepository<CgmToken, Long> {

    Optional<CgmToken> findByIsensUserId(String isensUserId);
}
