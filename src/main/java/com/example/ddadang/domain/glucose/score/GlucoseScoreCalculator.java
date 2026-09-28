package com.example.ddadang.domain.glucose.score;

import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties.GroupParams;
import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties.Score;
import com.example.ddadang.domain.glucose.score.GlucoseScore.Deductions;
import java.time.Duration;

/**
 * 혈당 점수 = max(0, 100 − TIR감점 − 평균혈당감점 − CV감점 − 스파이크감점 − 저혈당페널티).
 *
 * <p>평균 혈당(반올림 전 소수)과 스파이크 횟수는 기존 계산 결과를 그대로 입력받고, 이 클래스는 감점만 계산한다.
 * 감점은 구간표가 아니라 공식으로 계산하며, 감점 누적까지 소수로 유지하고 최종 점수 표시 시에만 반올림한다.
 * 모든 임계/계수는 {@link GlucoseAnalysisProperties}에서 관리한다.
 */
public final class GlucoseScoreCalculator {

    private GlucoseScoreCalculator() {
    }

    /**
     * @param inProgress 오늘(자정 전)이면 true → 잠정 점수, 최소 데이터 게이트 적용
     * @param spikeCount 측정 0건이면 null
     */
    public static GlucoseScore calculate(
        GlucoseAnalysisProperties properties, GlucoseGroup glucoseGroup,
        DailyGlucoseMetrics metrics, Integer spikeCount, boolean inProgress
    ) {
        Score score = properties.score();
        GroupParams group = properties.group(glucoseGroup);
        if (spikeCount == null || metrics.coveredMinutes() == 0) {
            return GlucoseScore.withoutScore(GlucoseScoreStatus.UNAVAILABLE);
        }
        boolean enoughData = metrics.coveragePercent() >= properties.mean().coverageMinPercent()
            && (!inProgress || metrics.coveredMinutes() >= minutes(score.provisionalMinCovered()));
        if (!enoughData) {
            return GlucoseScore.withoutScore(inProgress ? GlucoseScoreStatus.MEASURING : GlucoseScoreStatus.UNAVAILABLE);
        }

        Deductions deductions = new Deductions(
            tirDeduction(score, group, metrics.tirPercent()),
            meanDeduction(score, group, metrics.meanGlucose()),
            metrics.coveredMinutes() < minutes(score.cvMinCovered()) ? 0 : cvDeduction(score, group, metrics.cvPercent()),
            spikeDeduction(score, spikeCount),
            hypoPenalty(score, group, metrics.tbr1Percent(), metrics.tbr2Percent())
        );
        return new GlucoseScore(
            inProgress ? GlucoseScoreStatus.PROVISIONAL : GlucoseScoreStatus.FINAL,
            Math.max(0, 100 - deductions.total()),
            deductions
        );
    }

    /** min(tirMax, (100 − min(100, 달성률×100)) × tirWeight), 달성률 = 실제TIR ÷ 목표TIR */
    static double tirDeduction(Score score, GroupParams group, double tirPercent) {
        double achievement = tirPercent / group.tirTargetPercent();
        return Math.min(score.tirMax(), (100 - Math.min(100, achievement * 100)) * score.tirWeight());
    }

    /** min(meanMax, max(0, (평균 − start) × slope)) */
    static double meanDeduction(Score score, GroupParams group, double mean) {
        return Math.min(score.meanMax(), Math.max(0, (mean - group.meanStart()) * group.meanSlope()));
    }

    /** min(cvMax, max(0, (CV% − start) × slope)) */
    static double cvDeduction(Score score, GroupParams group, double cvPercent) {
        return Math.min(score.cvMax(), Math.max(0, (cvPercent - group.cvStart()) * group.cvSlope()));
    }

    /** min(spikeMax, 횟수 × spikePerCount) */
    static double spikeDeduction(Score score, int spikeCount) {
        return Math.min(score.spikeMax(), spikeCount * score.spikePerCount());
    }

    /** min(hypoMax, 초과TBR1%p × mildWeight + 초과TBR2%p × severeWeight) */
    static double hypoPenalty(Score score, GroupParams group, double tbr1Percent, double tbr2Percent) {
        double excess1 = Math.max(0, tbr1Percent - group.tbr1AllowedPercent());
        double excess2 = Math.max(0, tbr2Percent - group.tbr2AllowedPercent());
        return Math.min(score.hypoMax(), excess1 * score.hypoMildWeight() + excess2 * score.hypoSevereWeight());
    }

    private static double minutes(Duration duration) {
        return duration.toSeconds() / 60.0;
    }
}
