package com.example.ddadang.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 현재 시각 고정: 2026-09-29 10:00 KST.
 */
@TestConfiguration
public class FixedClockConfig {

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-09-29T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    }
}
