package com.example.ddadang.domain.glucose.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties.GroupParams;
import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties.Score;
import java.util.List;
import org.junit.jupiter.api.Test;

class GlucoseScoreCalculatorTest {

    private static final GlucoseAnalysisProperties PROPS = TestGlucoseProperties.PROPERTIES;
    private static final Score SCORE = PROPS.score();
    private static final GroupParams DM = PROPS.group(GlucoseGroup.DM);
    private static final GroupParams NON_DM = PROPS.group(GlucoseGroup.NON_DM);

    private static DailyGlucoseMetrics metrics(GlucoseGroup group, List<GlucoseSample> samples, int elapsedMinutes) {
        return DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_START.plusMinutes(elapsedMinutes), PROPS, group);
    }

    private static GlucoseScore finalScore(GlucoseGroup group, List<GlucoseSample> samples) {
        return GlucoseScoreCalculator.calculate(
            PROPS, group, metrics(group, samples, 1440), SpikeCounter.count(samples, PROPS.spike()), false
        );
    }

    @Test
    void 목표범위_안에서_안정적이면_100점_확정() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.fullDay(110));

        assertThat(score.status()).isEqualTo(GlucoseScoreStatus.FINAL);
        assertThat(score.score()).isEqualTo(100);
    }

    @Test
    void 감점은_구간표가_아니라_공식으로_계산해_소수를_유지한다() {
        // 스펙 예시: Non-DM 평균 104.3 → 표엔 −4지만 공식값 −4.3
        assertThat(GlucoseScoreCalculator.meanDeduction(SCORE, NON_DM, 104.3)).isCloseTo(4.3, within(1e-9));
        // DM: (136 − 120) × 0.29 = 4.64
        assertThat(GlucoseScoreCalculator.meanDeduction(SCORE, DM, 136)).isCloseTo(4.64, within(1e-9));
        assertThat(GlucoseScoreCalculator.meanDeduction(SCORE, NON_DM, 130)).isEqualTo(10);
    }

    @Test
    void 점수와_감점은_반올림하지_않은_소수로_누적된다() {
        GlucoseScore score = finalScore(GlucoseGroup.NON_DM, Samples.fullDay(104.3));

        assertThat(score.deductions().meanGlucose()).isCloseTo(4.3, within(1e-6));
        assertThat(score.score()).isCloseTo(95.7, within(1e-6));
    }

    @Test
    void TIR_감점은_목표_대비_달성률로_계산한다() {
        // DM 목표 70%, 실제 35% → 달성률 50% → 5점
        assertThat(GlucoseScoreCalculator.tirDeduction(SCORE, DM, 35)).isCloseTo(5, within(1e-9));
        assertThat(GlucoseScoreCalculator.tirDeduction(SCORE, DM, 80)).isZero();
        assertThat(GlucoseScoreCalculator.tirDeduction(SCORE, NON_DM, 0)).isEqualTo(10);
    }

    @Test
    void CV_감점은_15점이_상한() {
        assertThat(GlucoseScoreCalculator.cvDeduction(SCORE, DM, 24)).isCloseTo(6, within(1e-9));
        assertThat(GlucoseScoreCalculator.cvDeduction(SCORE, NON_DM, 60)).isEqualTo(15);
        assertThat(GlucoseScoreCalculator.cvDeduction(SCORE, NON_DM, 10)).isZero();
    }

    @Test
    void 스파이크는_회당_3점_최대_10점() {
        assertThat(GlucoseScoreCalculator.spikeDeduction(SCORE, 2)).isEqualTo(6);
        assertThat(GlucoseScoreCalculator.spikeDeduction(SCORE, 5)).isEqualTo(10);
    }

    @Test
    void 저혈당_페널티는_허용치_초과분에_가중치를_두고_20점이_상한() {
        // 스펙 표: TBR1 1%p 초과 → −2, 5%p+1%p → −14, 8%p+2%p → −20(캡)
        assertThat(GlucoseScoreCalculator.hypoPenalty(SCORE, DM, 5, 0)).isCloseTo(2, within(1e-9));
        assertThat(GlucoseScoreCalculator.hypoPenalty(SCORE, DM, 9, 2)).isCloseTo(14, within(1e-9));
        assertThat(GlucoseScoreCalculator.hypoPenalty(SCORE, DM, 12, 3)).isEqualTo(20);
    }

    @Test
    void 하루종일_경도_저혈당이면_TIR과_저혈당_감점이_모두_반영된다() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.fullDay(60));

        assertThat(score.deductions().tir()).isEqualTo(10);
        assertThat(score.deductions().hypoglycemia()).isEqualTo(20);
        assertThat(score.score()).isEqualTo(70);
    }

    @Test
    void 지난날_커버리지_70퍼센트_미만이면_미산출() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.every5Min(0, 900, m -> 110));

        assertThat(score.status()).isEqualTo(GlucoseScoreStatus.UNAVAILABLE);
        assertThat(score.score()).isNull();
    }

    @Test
    void 오늘은_3시간_미만이면_측정중_이상이면_잠정점수() {
        DailyGlucoseMetrics early = metrics(GlucoseGroup.DM, Samples.every5Min(0, 125, m -> 110), 125);
        assertThat(GlucoseScoreCalculator.calculate(PROPS, GlucoseGroup.DM, early, 0, true).status())
            .isEqualTo(GlucoseScoreStatus.MEASURING);

        DailyGlucoseMetrics later = metrics(GlucoseGroup.DM, Samples.every5Min(0, 245, m -> 110), 245);
        GlucoseScore provisional = GlucoseScoreCalculator.calculate(PROPS, GlucoseGroup.DM, later, 0, true);
        assertThat(provisional.status()).isEqualTo(GlucoseScoreStatus.PROVISIONAL);
        assertThat(provisional.score()).isEqualTo(100);
    }

    @Test
    void 측정_6시간_미만이면_CV_감점을_보류한다() {
        DailyGlucoseMetrics volatileFourHours = metrics(
            GlucoseGroup.DM, Samples.every5Min(0, 245, m -> m % 10 == 0 ? 80 : 170), 245
        );
        assertThat(volatileFourHours.cvPercent()).isGreaterThan(30);
        assertThat(GlucoseScoreCalculator.calculate(PROPS, GlucoseGroup.DM, volatileFourHours, 0, true)
            .deductions().cv()).isZero();
    }
}
