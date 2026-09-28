package com.example.ddadang.domain.glucose.score;

/**
 * @param averageGlucose 시간가중 평균(mg/dL). 측정 0건·커버리지 70% 미만·미래 날짜면 null
 * @param spikeCount     스파이크 횟수. 측정 0건·미래 날짜면 null
 */
public record DailyGlucoseAnalysis(
    Integer averageGlucose,
    Integer spikeCount,
    GlucoseScore score
) {

    public static DailyGlucoseAnalysis unavailable() {
        return new DailyGlucoseAnalysis(null, null, GlucoseScore.withoutScore(GlucoseScoreStatus.UNAVAILABLE));
    }
}
