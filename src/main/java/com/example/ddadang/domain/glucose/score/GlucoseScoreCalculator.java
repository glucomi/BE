package com.example.ddadang.domain.glucose.score;

import com.example.ddadang.domain.glucose.score.GlucoseScore.Deductions;

/**
 * 혈당 점수 = max(0, 100 − TIR감점 − 평균혈당감점 − CV감점 − 스파이크감점 − 저혈당페널티).
 * 감점은 구간 절벽 없이 연속값으로 계산하고 최종 점수만 반올림한다.
 */
public final class GlucoseScoreCalculator {

    static final double MIN_COVERAGE_PERCENT = 70;
    /** 진행 중(오늘) 점수 표시 최소 측정 시간. 스펙 "최소 3~4시간" 중 하한 */
    static final double MIN_COVERED_MINUTES_FOR_PROVISIONAL = 180;
    /** 측정 6시간 미만이면 CV 감점 보류 */
    static final double MIN_COVERED_MINUTES_FOR_CV = 360;

    private static final double TIR_MAX = 10;
    private static final double MEAN_MAX = 10;
    private static final double CV_MAX = 15;
    private static final double SPIKE_MAX = 10;
    private static final double SPIKE_PER_COUNT = 3;
    private static final double HYPO_MAX = 20;

    private GlucoseScoreCalculator() {
    }

    /**
     * @param inProgress 오늘(자정 전)이면 true → 잠정 점수, 최소 데이터 게이트 적용
     * @param spikeCount 측정 0건이면 null
     */
    public static GlucoseScore calculate(
        GlucoseGroup group, DailyGlucoseMetrics metrics, Integer spikeCount, boolean inProgress
    ) {
        if (spikeCount == null || metrics.coveredMinutes() == 0) {
            return GlucoseScore.withoutScore(GlucoseScoreStatus.UNAVAILABLE);
        }
        boolean enoughData = metrics.coveragePercent() >= MIN_COVERAGE_PERCENT
            && (!inProgress || metrics.coveredMinutes() >= MIN_COVERED_MINUTES_FOR_PROVISIONAL);
        if (!enoughData) {
            return GlucoseScore.withoutScore(inProgress ? GlucoseScoreStatus.MEASURING : GlucoseScoreStatus.UNAVAILABLE);
        }

        Deductions deductions = new Deductions(
            tirDeduction(group, metrics.tirPercent()),
            meanDeduction(group, metrics.meanGlucose()),
            metrics.coveredMinutes() < MIN_COVERED_MINUTES_FOR_CV ? 0 : cvDeduction(group, metrics.cvPercent()),
            Math.min(SPIKE_MAX, spikeCount * SPIKE_PER_COUNT),
            hypoPenalty(group, metrics.tbr1Percent(), metrics.tbr2Percent())
        );
        int score = (int) Math.max(0, Math.round(100 - deductions.total()));
        return new GlucoseScore(
            inProgress ? GlucoseScoreStatus.PROVISIONAL : GlucoseScoreStatus.FINAL, score, round(deductions)
        );
    }

    static double tirDeduction(GlucoseGroup group, double tirPercent) {
        double achievement = tirPercent / group.getTirTargetPercent();
        return Math.min(TIR_MAX, (100 - Math.min(100, achievement * 100)) * 0.10);
    }

    static double meanDeduction(GlucoseGroup group, double mean) {
        return Math.min(MEAN_MAX, Math.max(0, (mean - group.getMeanStart()) * group.getMeanSlope()));
    }

    static double cvDeduction(GlucoseGroup group, double cvPercent) {
        return Math.min(CV_MAX, Math.max(0, (cvPercent - group.getCvStart()) * group.getCvSlope()));
    }

    static double hypoPenalty(GlucoseGroup group, double tbr1Percent, double tbr2Percent) {
        double excess1 = Math.max(0, tbr1Percent - group.getTbr1AllowedPercent());
        double excess2 = Math.max(0, tbr2Percent - group.getTbr2AllowedPercent());
        return Math.min(HYPO_MAX, excess1 * 2 + excess2 * 4);
    }

    private static Deductions round(Deductions d) {
        return new Deductions(
            round1(d.tir()), round1(d.meanGlucose()), round1(d.cv()), round1(d.spike()), round1(d.hypoglycemia())
        );
    }

    private static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
