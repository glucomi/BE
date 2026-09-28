package com.example.ddadang.domain.glucose.score;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

final class Samples {

    static final OffsetDateTime DAY_START = OffsetDateTime.parse("2026-09-28T00:00:00+09:00");
    static final OffsetDateTime DAY_END = DAY_START.plusDays(1);

    private Samples() {
    }

    /** DAY_START + fromMinute부터 toMinute(미포함)까지 5분 간격, 값은 분 단위 함수 */
    static List<GlucoseSample> every5Min(int fromMinute, int toMinute, IntToDoubleFunction valueAtMinute) {
        List<GlucoseSample> samples = new ArrayList<>();
        for (int minute = fromMinute; minute < toMinute; minute += 5) {
            samples.add(new GlucoseSample(DAY_START.plusMinutes(minute), valueAtMinute.applyAsDouble(minute)));
        }
        return samples;
    }

    static List<GlucoseSample> fullDay(double value) {
        return every5Min(0, 1440, minute -> value);
    }
}
