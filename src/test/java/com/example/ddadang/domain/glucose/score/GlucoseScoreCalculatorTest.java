package com.example.ddadang.domain.glucose.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

class GlucoseScoreCalculatorTest {

    private static GlucoseScore finalScore(GlucoseGroup group, List<GlucoseSample> samples) {
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_END, group);
        return GlucoseScoreCalculator.calculate(group, metrics, SpikeCounter.count(samples), false);
    }

    @Test
    void 목표범위_안에서_안정적이면_100점_확정() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.fullDay(110));

        assertThat(score.status()).isEqualTo(GlucoseScoreStatus.FINAL);
        assertThat(score.score()).isEqualTo(100);
    }

    @Test
    void 평균혈당_감점은_그룹별_start_slope를_따르고_10점이_상한() {
        assertThat(finalScore(GlucoseGroup.NON_DM, Samples.fullDay(105)).score()).isEqualTo(95);
        assertThat(finalScore(GlucoseGroup.NON_DM, Samples.fullDay(130)).deductions().meanGlucose()).isEqualTo(10);
        // DM: (136-120)*0.29 = 4.64
        assertThat(GlucoseScoreCalculator.meanDeduction(GlucoseGroup.DM, 136)).isCloseTo(4.64, within(0.001));
    }

    @Test
    void TIR_감점은_목표_대비_달성률로_계산한다() {
        // DM 목표 70%, 실제 35% → 달성률 50% → 5점
        assertThat(GlucoseScoreCalculator.tirDeduction(GlucoseGroup.DM, 35)).isCloseTo(5, within(0.001));
        assertThat(GlucoseScoreCalculator.tirDeduction(GlucoseGroup.DM, 80)).isZero();
        assertThat(GlucoseScoreCalculator.tirDeduction(GlucoseGroup.NON_DM, 0)).isEqualTo(10);
    }

    @Test
    void CV_감점은_15점이_상한() {
        assertThat(GlucoseScoreCalculator.cvDeduction(GlucoseGroup.DM, 24)).isCloseTo(6, within(0.001));
        assertThat(GlucoseScoreCalculator.cvDeduction(GlucoseGroup.NON_DM, 60)).isEqualTo(15);
        assertThat(GlucoseScoreCalculator.cvDeduction(GlucoseGroup.NON_DM, 10)).isZero();
    }

    @Test
    void 저혈당_페널티는_허용치_초과분에_가중치를_두고_20점이_상한() {
        assertThat(GlucoseScoreCalculator.hypoPenalty(GlucoseGroup.DM, 5, 0)).isCloseTo(2, within(0.001));
        assertThat(GlucoseScoreCalculator.hypoPenalty(GlucoseGroup.DM, 9, 2)).isCloseTo(14, within(0.001));
        assertThat(GlucoseScoreCalculator.hypoPenalty(GlucoseGroup.DM, 12, 3)).isEqualTo(20);
    }

    @Test
    void 하루종일_경도_저혈당이면_TIR과_저혈당_감점이_모두_반영된다() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.fullDay(60));

        assertThat(score.deductions().tir()).isEqualTo(10);
        assertThat(score.deductions().hypoglycemia()).isEqualTo(20);
        assertThat(score.score()).isEqualTo(70);
    }

    @Test
    void 스파이크는_회당_3점_최대_10점() {
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(
            Samples.fullDay(110), Samples.DAY_START, Samples.DAY_END, GlucoseGroup.DM
        );
        assertThat(GlucoseScoreCalculator.calculate(GlucoseGroup.DM, metrics, 2, false).deductions().spike()).isEqualTo(6);
        assertThat(GlucoseScoreCalculator.calculate(GlucoseGroup.DM, metrics, 5, false).deductions().spike()).isEqualTo(10);
    }

    @Test
    void 지난날_커버리지_70퍼센트_미만이면_미산출() {
        GlucoseScore score = finalScore(GlucoseGroup.DM, Samples.every5Min(0, 900, m -> 110));

        assertThat(score.status()).isEqualTo(GlucoseScoreStatus.UNAVAILABLE);
        assertThat(score.score()).isNull();
    }

    @Test
    void 오늘은_3시간_미만이면_측정중_이상이면_잠정점수() {
        List<GlucoseSample> twoHours = Samples.every5Min(0, 125, m -> 110);
        DailyGlucoseMetrics early = DailyGlucoseMetrics.of(
            twoHours, Samples.DAY_START, Samples.DAY_START.plusMinutes(125), GlucoseGroup.DM
        );
        assertThat(GlucoseScoreCalculator.calculate(GlucoseGroup.DM, early, 0, true).status())
            .isEqualTo(GlucoseScoreStatus.MEASURING);

        List<GlucoseSample> fourHours = Samples.every5Min(0, 245, m -> 110);
        DailyGlucoseMetrics later = DailyGlucoseMetrics.of(
            fourHours, Samples.DAY_START, Samples.DAY_START.plusMinutes(245), GlucoseGroup.DM
        );
        GlucoseScore provisional = GlucoseScoreCalculator.calculate(GlucoseGroup.DM, later, 0, true);
        assertThat(provisional.status()).isEqualTo(GlucoseScoreStatus.PROVISIONAL);
        assertThat(provisional.score()).isEqualTo(100);
    }

    @Test
    void 측정_6시간_미만이면_CV_감점을_보류한다() {
        List<GlucoseSample> volatileFourHours = Samples.every5Min(0, 245, m -> m % 10 == 0 ? 80 : 170);
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(
            volatileFourHours, Samples.DAY_START, Samples.DAY_START.plusMinutes(245), GlucoseGroup.DM
        );
        assertThat(metrics.cvPercent()).isGreaterThan(30);
        assertThat(GlucoseScoreCalculator.calculate(GlucoseGroup.DM, metrics, 0, true).deductions().cv()).isZero();
    }
}
