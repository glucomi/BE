package com.example.ddadang.domain.glucose.score;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SpikeCounterTest {

    /** 12:00 기준 offset분에 120 → 30분에 걸쳐 200까지 상승, holdMinutes 동안 200 유지 후 복귀 */
    private static double spikeAt(int minute, int startMinute, int holdMinutes) {
        int t = minute - startMinute;
        if (t < 0) {
            return 120;
        }
        if (t <= 30) {
            return 120 + t * (80.0 / 30);
        }
        if (t <= 30 + holdMinutes) {
            return 200;
        }
        return 120;
    }

    @Test
    void 측정이_없으면_null() {
        assertThat(SpikeCounter.count(List.of())).isNull();
    }

    @Test
    void 평탄하면_0회() {
        assertThat(SpikeCounter.count(Samples.fullDay(130))).isZero();
    }

    @Test
    void 급상승해_180_이상을_15분_이상_유지하면_1회() {
        List<GlucoseSample> samples = Samples.every5Min(0, 1440, m -> spikeAt(m, 720, 20));
        assertThat(SpikeCounter.count(samples)).isEqualTo(1);
    }

    @Test
    void 고혈당_구간이_15분_미만이면_제외() {
        // 180 이상: 26분(186.7)~30분 + 5분 유지 → 25~35분 사이 약 10분
        List<GlucoseSample> samples = Samples.every5Min(0, 1440, m -> spikeAt(m, 720, 5));
        assertThat(SpikeCounter.count(samples)).isZero();
    }

    @Test
    void 완만하게_상승해_60분_내_상승폭이_50_미만이면_제외() {
        // 150에서 5분마다 +3씩 천천히 상승 → 60분 상승폭 36
        List<GlucoseSample> samples = Samples.every5Min(0, 1440, m -> m < 600 ? 150 : Math.min(230, 150 + (m - 600) / 5 * 3.0));
        assertThat(SpikeCounter.count(samples)).isZero();
    }

    @Test
    void 직전_스파이크_시작과_120분_미만이면_병합하고_이후는_별도로_센다() {
        List<GlucoseSample> merged = Samples.every5Min(0, 1440,
            m -> Math.max(spikeAt(m, 480, 20), spikeAt(m, 560, 20)));
        assertThat(SpikeCounter.count(merged)).isEqualTo(1);

        List<GlucoseSample> separate = Samples.every5Min(0, 1440,
            m -> Math.max(spikeAt(m, 480, 20), spikeAt(m, 720, 20)));
        assertThat(SpikeCounter.count(separate)).isEqualTo(2);
    }

    @Test
    void 결측이_15분을_넘으면_구간을_나눠_판정한다() {
        // 180 이상 구간 사이에 30분 결측 → 각 구간 10분이라 지속 조건 미달
        List<GlucoseSample> samples = new java.util.ArrayList<>(Samples.every5Min(600, 660, m -> m < 650 ? 120 : 200));
        samples.addAll(Samples.every5Min(690, 700, m -> 200));
        assertThat(SpikeCounter.count(samples)).isZero();
    }
}
