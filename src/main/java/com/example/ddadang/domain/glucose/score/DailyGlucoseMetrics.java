package com.example.ddadang.domain.glucose.score;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 하루(지난 날: 00:00~24:00 / 오늘: 00:00~현재) 구간의 시간가중 혈당 지표.
 *
 * <p>평균 혈당 스펙(시간가중 평균)과 같은 방식으로 인접 측정 쌍을 적분한다. 간격이
 * mean.gapThreshold를 넘는 쌍은 결측으로 보고 면적·시간 모두 제외한다(보간/hold 없음).
 * 사다리꼴 적분은 각 쌍의 시간을 양 끝 측정값에 절반씩 나눠 준 가중 평균과 같으므로,
 * TIR/TBR/CV도 같은 가중치로 계산해 평균과 일관되게 맞춘다.
 * 모든 % 지표(TIR/TBR/커버리지)의 분모는 경과 시간(elapsedMinutes)이다.
 */
public record DailyGlucoseMetrics(
    double elapsedMinutes,
    double coveredMinutes,
    double meanGlucose,
    double cvPercent,
    double tirPercent,
    double tbr1Percent,
    double tbr2Percent
) {

    public double coveragePercent() {
        return elapsedMinutes == 0 ? 0 : coveredMinutes / elapsedMinutes * 100;
    }

    /**
     * @param samples {@link GlucoseSamples#clean}으로 전처리된 측정값
     * @param end     지난 날이면 다음날 00:00, 오늘이면 현재 시각
     */
    public static DailyGlucoseMetrics of(
        List<GlucoseSample> samples, OffsetDateTime start, OffsetDateTime end,
        GlucoseAnalysisProperties properties, GlucoseGroup glucoseGroup
    ) {
        GlucoseAnalysisProperties.GroupParams group = properties.group(glucoseGroup);
        double gapThresholdMinutes = properties.mean().gapThreshold().toSeconds() / 60.0;
        double severeHypoBelow = properties.score().severeHypoBelow();
        double[] weights = new double[samples.size()];
        double covered = 0;
        for (int i = 0; i + 1 < samples.size(); i++) {
            double dt = minutesBetween(samples.get(i).time(), samples.get(i + 1).time());
            if (dt <= 0 || dt > gapThresholdMinutes) {
                continue;
            }
            weights[i] += dt / 2;
            weights[i + 1] += dt / 2;
            covered += dt;
        }

        double weightedSum = 0;
        double inRange = 0;
        double mildHypo = 0;
        double severeHypo = 0;
        for (int i = 0; i < samples.size(); i++) {
            double value = samples.get(i).value();
            double weight = weights[i];
            weightedSum += value * weight;
            if (value >= group.rangeLow() && value <= group.rangeHigh()) {
                inRange += weight;
            }
            if (value < severeHypoBelow) {
                severeHypo += weight;
            } else if (value < group.mildHypoBelow()) {
                mildHypo += weight;
            }
        }

        double mean = covered == 0 ? 0 : weightedSum / covered;
        double variance = 0;
        for (int i = 0; i < samples.size(); i++) {
            variance += weights[i] * Math.pow(samples.get(i).value() - mean, 2);
        }
        double sd = covered == 0 ? 0 : Math.sqrt(variance / covered);
        double elapsed = minutesBetween(start, end);

        return new DailyGlucoseMetrics(
            elapsed,
            covered,
            mean,
            mean == 0 ? 0 : sd / mean * 100,
            percent(inRange, elapsed),
            percent(mildHypo, elapsed),
            percent(severeHypo, elapsed)
        );
    }

    private static double minutesBetween(OffsetDateTime from, OffsetDateTime to) {
        return Duration.between(from, to).toSeconds() / 60.0;
    }

    private static double percent(double minutes, double elapsed) {
        return elapsed == 0 ? 0 : minutes / elapsed * 100;
    }
}
