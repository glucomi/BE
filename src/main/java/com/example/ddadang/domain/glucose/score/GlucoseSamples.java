package com.example.ddadang.domain.glucose.score;

import com.example.ddadang.domain.glucose.entity.CgmReading;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 평균 혈당/스파이크 공통 전처리: [start, end) 필터 → 시각 오름차순 → null·오류·센서 유효 범위 밖 제거 → 동일 시각 중복 제거.
 */
public final class GlucoseSamples {

    private GlucoseSamples() {
    }

    public static List<GlucoseSample> clean(
        List<CgmReading> readings, OffsetDateTime start, OffsetDateTime end, GlucoseAnalysisProperties.Sensor sensor
    ) {
        Map<Instant, GlucoseSample> byInstant = new LinkedHashMap<>();
        readings.stream()
            .filter(reading -> !reading.getEventAt().isBefore(start) && reading.getEventAt().isBefore(end))
            .filter(reading -> reading.getValue() != null)
            .filter(reading -> reading.getErrorCode() == null || reading.getErrorCode() == 0)
            .filter(reading -> reading.getValue() >= sensor.bgMin() && reading.getValue() <= sensor.bgMax())
            .sorted(Comparator.comparing(CgmReading::getEventAt))
            .forEach(reading -> byInstant.putIfAbsent(
                reading.getEventAt().toInstant(), new GlucoseSample(reading.getEventAt(), reading.getValue())
            ));
        return List.copyOf(byInstant.values());
    }
}
