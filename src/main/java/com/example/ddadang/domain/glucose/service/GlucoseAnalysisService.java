package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.score.DailyGlucoseAnalysis;
import com.example.ddadang.domain.glucose.score.DailyGlucoseMetrics;
import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties;
import com.example.ddadang.domain.glucose.score.GlucoseGroup;
import com.example.ddadang.domain.glucose.score.GlucoseSample;
import com.example.ddadang.domain.glucose.score.GlucoseSamples;
import com.example.ddadang.domain.glucose.score.GlucoseScore;
import com.example.ddadang.domain.glucose.score.GlucoseScoreCalculator;
import com.example.ddadang.domain.glucose.score.SpikeCounter;
import com.example.ddadang.domain.member.enums.DiabetesType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * 하루 혈당 요약 지표(평균 혈당, 스파이크, 혈당 점수)를 계산한다.
 * 지난 날은 00:00~24:00 확정값, 오늘은 00:00~현재 기준 잠정값이며 반환값은 반올림 전 소수다.
 * TODO: 자정 이후 확정 점수를 저장(freeze)하는 배치는 추후 추가. 지금은 조회 시점에 계산한다.
 */
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(GlucoseAnalysisProperties.class)
public class GlucoseAnalysisService {

    private final GlucoseAnalysisProperties properties;

    public DailyGlucoseAnalysis analyze(
        DiabetesType diabetesType, List<CgmReading> readings, LocalDate date, ZoneId zone, OffsetDateTime now
    ) {
        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        if (!now.isAfter(dayStart)) {
            return DailyGlucoseAnalysis.unavailable();
        }
        boolean inProgress = now.isBefore(dayEnd);
        OffsetDateTime end = inProgress ? now : dayEnd;

        GlucoseGroup group = GlucoseGroup.from(diabetesType);
        List<GlucoseSample> samples = GlucoseSamples.clean(readings, dayStart, end, properties.sensor());
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(samples, dayStart, end, properties, group);
        Integer spikeCount = SpikeCounter.count(samples, properties.spike());
        GlucoseScore score = GlucoseScoreCalculator.calculate(properties, group, metrics, spikeCount, inProgress);

        boolean meanAvailable = metrics.coveredMinutes() > 0
            && metrics.coveragePercent() >= properties.mean().coverageMinPercent();
        return new DailyGlucoseAnalysis(meanAvailable ? metrics.meanGlucose() : null, spikeCount, score);
    }
}
