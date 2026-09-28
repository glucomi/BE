package com.example.ddadang.domain.glucose.score;

public enum GlucoseScoreStatus {
    /** 지난 날짜, 확정 점수 */
    FINAL,
    /** 오늘, 00:00~현재 기준 잠정 점수 */
    PROVISIONAL,
    /** 오늘이지만 최소 데이터(커버리지 70%, 3시간) 미달 → "측정 중" */
    MEASURING,
    /** 측정 0건, 커버리지 70% 미만, 미래 날짜 → 점수 미산출 */
    UNAVAILABLE
}
