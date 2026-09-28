package com.example.ddadang.domain.glucose.score;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 평균 혈당 · 스파이크 · 혈당 점수 계산 파라미터. 값은 glucose-analysis.yml에서 관리한다(실데이터로 튜닝 예정).
 */
@ConfigurationProperties(prefix = "glucose.analysis")
public record GlucoseAnalysisProperties(
    Sensor sensor,
    Mean mean,
    Spike spike,
    Score score,
    Map<GlucoseGroup, GroupParams> groups
) {

    public GroupParams group(GlucoseGroup group) {
        GroupParams params = groups.get(group);
        if (params == null) {
            throw new IllegalStateException("glucose.analysis.groups." + group + " 설정이 없습니다.");
        }
        return params;
    }

    /** 센서 유효 범위(mg/dL) */
    public record Sensor(double bgMin, double bgMax) {
    }

    /** 시간가중 평균: gapThreshold 초과 쌍은 결측, 커버리지 기준 미만이면 미표시 */
    public record Mean(Duration gapThreshold, double coverageMinPercent) {
    }

    public record Spike(
        double peakMin,
        double riseThreshold,
        Duration window,
        Duration sustain,
        Duration mergeInterval,
        Duration gapSkip
    ) {
    }

    public record Score(
        Duration provisionalMinCovered,
        Duration cvMinCovered,
        double tirMax,
        double tirWeight,
        double meanMax,
        double cvMax,
        double spikePerCount,
        double spikeMax,
        double severeHypoBelow,
        double hypoMildWeight,
        double hypoSevereWeight,
        double hypoMax
    ) {
    }

    /**
     * 그룹별 목표 범위/허용치/감점 계수. 경도 저혈당(TBR1)은 severeHypoBelow ≤ 값 < mildHypoBelow.
     */
    public record GroupParams(
        double rangeLow,
        double rangeHigh,
        double tirTargetPercent,
        double tbr1AllowedPercent,
        double tbr2AllowedPercent,
        double mildHypoBelow,
        double meanStart,
        double meanSlope,
        double cvStart,
        double cvSlope
    ) {
    }
}
