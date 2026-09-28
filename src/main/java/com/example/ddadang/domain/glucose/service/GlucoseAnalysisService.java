package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.score.DailyGlucoseAnalysis;
import com.example.ddadang.domain.glucose.score.DailyGlucoseMetrics;
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
import org.springframework.stereotype.Service;

/**
 * 하루 혈당 요약 지표(평균 혈당, 스파이크, 혈당 점수)를 계산한다.
 * 지난 날은 00:00~24:00 확정값, 오늘은 00:00~현재 기준 잠정값이다.
 * TODO: 자정 이후 확정 점수를 저장(freeze)하는 배치는 추후 추가. 지금은 조회 시점에 계산한다.
 */
@Service
public class GlucoseAnalysisService {

    private static final double MIN_COVERAGE_PERCENT_FOR_MEAN = 70;

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
        List<GlucoseSample> samples = GlucoseSamples.clean(readings, dayStart, end);
        DailyGlucoseMetrics metrics = DailyGlucoseMetrics.of(samples, dayStart, end, group);
        Integer spikeCount = SpikeCounter.count(samples);
        GlucoseScore score = GlucoseScoreCalculator.calculate(group, metrics, spikeCount, inProgress);

        Integer average = metrics.coveredMinutes() == 0 || metrics.coveragePercent() < MIN_COVERAGE_PERCENT_FOR_MEAN
            ? null
            : (int) Math.round(metrics.meanGlucose());
        return new DailyGlucoseAnalysis(average, spikeCount, score);
    }
}
