package com.example.ddadang.domain.glucose.score;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 혈당 스파이크 횟수(count_spikes 스펙). 임계값은 {@link GlucoseAnalysisProperties.Spike}.
 * 피크 전 window 내 riseThreshold 이상 급상승해 peakMin 이상이 되고 sustain 이상 유지되면 1회,
 * 직전 스파이크 시작과 mergeInterval 미만이면 병합한다.
 */
public final class SpikeCounter {

    private SpikeCounter() {
    }

    /**
     * @param samples {@link GlucoseSamples#clean}으로 전처리된 측정값
     * @return 스파이크 횟수, 측정 0건이면 null
     */
    public static Integer count(List<GlucoseSample> samples, GlucoseAnalysisProperties.Spike spike) {
        if (samples.isEmpty()) {
            return null;
        }

        int count = 0;
        OffsetDateTime lastSpikeStart = null;
        for (List<GlucoseSample> segment : highSegments(samples, spike)) {
            OffsetDateTime segmentStart = segment.get(0).time();
            OffsetDateTime segmentEnd = segment.get(segment.size() - 1).time();
            if (Duration.between(segmentStart, segmentEnd).compareTo(spike.sustain()) < 0) {
                continue;
            }

            GlucoseSample peak = segment.stream().max(Comparator.comparingDouble(GlucoseSample::value)).orElseThrow();
            double low = samples.stream()
                .filter(s -> !s.time().isBefore(peak.time().minus(spike.window())) && !s.time().isAfter(peak.time()))
                .mapToDouble(GlucoseSample::value)
                .min()
                .orElse(peak.value());
            if (peak.value() - low < spike.riseThreshold()) {
                continue;
            }

            if (lastSpikeStart != null
                && Duration.between(lastSpikeStart, segmentStart).compareTo(spike.mergeInterval()) < 0) {
                continue;
            }
            count++;
            lastSpikeStart = segmentStart;
        }
        return count;
    }

    /**
     * peakMin 이상인 연속 구간. 구간 안에서 측정 간격이 gapSkip을 넘으면 구간을 나눈다.
     */
    private static List<List<GlucoseSample>> highSegments(
        List<GlucoseSample> samples, GlucoseAnalysisProperties.Spike spike
    ) {
        List<List<GlucoseSample>> segments = new ArrayList<>();
        List<GlucoseSample> current = new ArrayList<>();
        for (GlucoseSample sample : samples) {
            if (sample.value() >= spike.peakMin()) {
                if (!current.isEmpty() && Duration.between(current.get(current.size() - 1).time(), sample.time())
                    .compareTo(spike.gapSkip()) > 0) {
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
