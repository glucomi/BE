package com.example.ddadang.domain.home.service;

import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.score.DailyGlucoseAnalysis;
import com.example.ddadang.domain.glucose.service.GlucoseAnalysisService;
import com.example.ddadang.domain.glucose.service.GlucoseQueryService;
import com.example.ddadang.domain.home.dto.HomeResponse;
import com.example.ddadang.domain.home.dto.HomeResponse.Graph;
import com.example.ddadang.domain.home.dto.HomeResponse.Meal;
import com.example.ddadang.domain.home.dto.HomeResponse.Point;
import com.example.ddadang.domain.home.dto.HomeResponse.Sensor;
import com.example.ddadang.domain.home.dto.HomeResponse.Summary;
import com.example.ddadang.domain.home.dto.HomeResponse.TargetRange;
import com.example.ddadang.domain.member.dto.response.MemberResponse;
import com.example.ddadang.domain.member.service.MemberService;
import com.example.ddadang.domain.record.meal.dto.response.MealRecordResponse;
import com.example.ddadang.domain.record.meal.service.MealRecordService;
import com.example.ddadang.global.config.ClockConfig;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.IntSummaryStatistics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 홈 화면(MO-HOME-010)에 필요한 회원/혈당/식사 데이터를 날짜(KST 하루) 기준으로 조합한다.
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private static final ZoneId KST = ClockConfig.KST;

    private final MemberService memberService;
    private final GlucoseQueryService glucoseQueryService;
    private final GlucoseAnalysisService glucoseAnalysisService;
    private final MealRecordService mealRecordService;
    private final Clock clock;

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public HomeResponse getHome(Long memberId, LocalDate date) {
        MemberResponse member = memberService.getMe(memberId);
        OffsetDateTime from = date.atStartOfDay(KST).toOffsetDateTime();
        OffsetDateTime to = date.plusDays(1).atStartOfDay(KST).toOffsetDateTime();

        List<CgmReading> readings = glucoseQueryService.getReadings(memberId, from, to);
        List<MealRecordResponse> meals = mealRecordService.getMealRecords(
            memberId, from.toLocalDateTime(), to.toLocalDateTime()
        );

        DailyGlucoseAnalysis analysis = glucoseAnalysisService.analyze(
            member.diabetesType(), readings, date, KST, OffsetDateTime.now(clock)
        );

        IntSummaryStatistics stats = readings.stream()
            .mapToInt(reading -> (int) Math.round(reading.getValue()))
            .summaryStatistics();
        boolean hasReadings = stats.getCount() > 0;
        Integer max = hasReadings ? stats.getMax() : null;

        return new HomeResponse(
            date,
            toSensor(memberId, date),
            member.targetGlucoseMin() == null
                ? null : new TargetRange(member.targetGlucoseMin(), member.targetGlucoseMax()),
            new Graph(toPoints(readings), hasReadings ? stats.getMin() : null, max),
            new Summary(
                analysis.score().score(),
                analysis.score().status(),
                analysis.score().deductions(),
                max,
                analysis.averageGlucose(),
                analysis.spikeCount(),
                sumCarbohydrate(meals)
            ),
            meals.stream().map(this::toMeal).toList()
        );
    }

    private Sensor toSensor(Long memberId, LocalDate date) {
        return glucoseQueryService.findConnectedCgm(memberId)
            .map(connection -> new Sensor(true, connection.getProvider(), sensorDay(connection, date)))
            .orElseGet(Sensor::disconnected);
    }

    private Integer sensorDay(CgmConnection connection, LocalDate date) {
        if (connection.getSensorStartedAt() == null) {
            return null;
        }
        LocalDate startedOn = connection.getSensorStartedAt().atZoneSameInstant(KST).toLocalDate();
        long days = ChronoUnit.DAYS.between(startedOn, date) + 1;
        return days < 1 ? null : (int) days;
    }

    private List<Point> toPoints(List<CgmReading> readings) {
        return readings.stream()
            .map(reading -> new Point(
                reading.getEventAt().atZoneSameInstant(KST).toOffsetDateTime(),
                (int) Math.round(reading.getValue()),
                reading.getTrend()
            ))
            .toList();
    }

    private BigDecimal sumCarbohydrate(List<MealRecordResponse> meals) {
        return meals.stream()
            .map(meal -> meal.total().carbohydrateG())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Meal toMeal(MealRecordResponse meal) {
        String representative = meal.items().isEmpty() ? null : meal.items().get(0).name();
        return new Meal(
            meal.mealRecordId(), meal.eatenAt(), representative, meal.items().size(),
            meal.total().kcal(), meal.total().carbohydrateG()
        );
    }
}
