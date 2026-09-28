package com.example.ddadang.domain.glucose.score;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 혈당 스파이크 횟수(count_spikes 스펙).
 * 최근 60분 내 50 이상 급상승해 180 이상이 되고 15분 이상 유지되면 1회, 120분 내 재발은 병합한다.
 */
public final class SpikeCounter {

    static final double PEAK_MIN = 180;
    static final double RISE_THRESHOLD = 50;
    static final Duration WINDOW = Duration.ofMinutes(60);
    static final Duration SUSTAIN = Duration.ofMinutes(15);
    static final Duration MERGE_INTERVAL = Duration.ofMinutes(120);
    static final Duration GAP_SKIP = Duration.ofMinutes(15);

    private SpikeCounter() {
    }

    /**
     * @param samples {@link GlucoseSamples#clean}으로 전처리된 측정값
     * @return 스파이크 횟수, 측정 0건이면 null
     */
    public static Integer count(List<GlucoseSample> samples) {
        if (samples.isEmpty()) {
            return null;
        }

        int count = 0;
        OffsetDateTime lastSpikeStart = null;
        for (List<GlucoseSample> segment : highSegments(samples)) {
            OffsetDateTime segmentStart = segment.get(0).time();
            OffsetDateTime segmentEnd = segment.get(segment.size() - 1).time();
            if (Duration.between(segmentStart, segmentEnd).compareTo(SUSTAIN) < 0) {
                continue;
            }

            GlucoseSample peak = segment.stream().max(Comparator.comparingDouble(GlucoseSample::value)).orElseThrow();
            double low = samples.stream()
                .filter(s -> !s.time().isBefore(peak.time().minus(WINDOW)) && !s.time().isAfter(peak.time()))
                .mapToDouble(GlucoseSample::value)
                .min()
                .orElse(peak.value());
            if (peak.value() - low < RISE_THRESHOLD) {
                continue;
            }

            if (lastSpikeStart != null && Duration.between(lastSpikeStart, segmentStart).compareTo(MERGE_INTERVAL) < 0) {
                continue;
            }
            count++;
            lastSpikeStart = segmentStart;
        }
        return count;
    }

    /**
     * 180 이상인 연속 구간. 구간 안에서 측정 간격이 GAP_SKIP을 넘으면 구간을 나눈다.
     */
    private static List<List<GlucoseSample>> highSegments(List<GlucoseSample> samples) {
        List<List<GlucoseSample>> segments = new ArrayList<>();
        List<GlucoseSample> current = new ArrayList<>();
        for (GlucoseSample sample : samples) {
            if (sample.value() >= PEAK_MIN) {
                if (!current.isEmpty()
                    && Duration.between(current.get(current.size() - 1).time(), sample.time()).compareTo(GAP_SKIP) > 0) {
                    segments.add(current);
                    current = new ArrayList<>();
                }
                current.add(sample);
            } else if (!current.isEmpty()) {
                segments.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            segments.add(current);
        }
        return segments;
    }
}
