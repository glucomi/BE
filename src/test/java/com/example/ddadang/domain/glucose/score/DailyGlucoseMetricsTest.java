package com.example.ddadang.domain.glucose.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyGlucoseMetricsTest {

    @Test
    void 사다리꼴_적분으로_시간가중_평균을_구한다() {
        // 00:00 100, 00:05 200 → 평균 150 (area/covered)
        List<GlucoseSample> samples = Samples.every5Min(0, 10, m -> m == 0 ? 100 : 200);
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_END, GlucoseGroup.DM);

        assertThat(metrics.meanGlucose()).isEqualTo(150);
        assertThat(metrics.coveredMinutes()).isEqualTo(5);
    }

    @Test
    void 간격이_20분을_넘는_구간은_적분에서_제외하고_분모에는_포함한다() {
        List<GlucoseSample> samples = new ArrayList<>(Samples.every5Min(0, 60, m -> 100));
        samples.addAll(Samples.every5Min(120, 180, m -> 200));
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_END, GlucoseGroup.DM);

        assertThat(metrics.coveredMinutes()).isEqualTo(110);
        assertThat(metrics.meanGlucose()).isEqualTo(150);
        assertThat(metrics.coveragePercent()).isCloseTo(110 / 1440.0 * 100, within(0.001));
    }

    @Test
    void TIR과_저혈당_비율은_그룹_기준과_경과시간으로_계산한다() {
        // 하루의 절반 60(경도 저혈당), 절반 100
        List<GlucoseSample> samples = Samples.every5Min(0, 1440, m -> m < 720 ? 60 : 100);
        DailyGlucoseMetrics dm = DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_END, GlucoseGroup.DM);
        DailyGlucoseMetrics gdm = DailyGlucoseMetrics.of(samples, Samples.DAY_START, Samples.DAY_END, GlucoseGroup.GDM);

        assertThat(dm.tbr1Percent()).isCloseTo(50, within(0.5));
        assertThat(dm.tirPercent()).isCloseTo(50, within(0.5));
        // GDM은 63 미만부터 저혈당, 목표범위 하한 63 → 60은 동일하게 저혈당
        assertThat(gdm.tbr1Percent()).isCloseTo(50, within(0.5));
        assertThat(dm.tbr2Percent()).isZero();
    }

    @Test
    void 오늘은_경과시간을_분모로_쓴다() {
        List<GlucoseSample> samples = Samples.every5Min(0, 245, m -> 100);
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(
            samples, Samples.DAY_START, Samples.DAY_START.plusMinutes(245), GlucoseGroup.DM
        );

        assertThat(metrics.coveragePercent()).isCloseTo(240 / 245.0 * 100, within(0.001));
    }
}
