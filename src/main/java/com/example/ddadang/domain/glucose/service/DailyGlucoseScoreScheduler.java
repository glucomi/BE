package com.example.ddadang.domain.glucose.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 날짜별 혈당 점수 확정 배치. 실행 주기는 glucose.analysis.freeze.cron.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "glucose.score-freeze.scheduler-enabled", havingValue = "true")
public class DailyGlucoseScoreScheduler {

    private final DailyGlucoseScoreService dailyGlucoseScoreService;

    @Scheduled(cron = "${glucose.analysis.freeze.cron}", zone = "Asia/Seoul")
    public void freeze() {
        int frozen = dailyGlucoseScoreService.freezeEligibleDays();
        if (frozen > 0) {
            log.info("혈당 점수 {}건을 확정 저장했습니다.", frozen);
        }
    }
}
