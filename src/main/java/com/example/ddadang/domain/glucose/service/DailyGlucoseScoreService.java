package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.dto.response.DailyScoreResponse;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.entity.DailyGlucoseScore;
import com.example.ddadang.domain.glucose.enums.CgmConnectionStatus;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.repository.CgmReadingRepository;
import com.example.ddadang.domain.glucose.repository.DailyGlucoseScoreRepository;
import com.example.ddadang.domain.glucose.score.DailyGlucoseAnalysis;
import com.example.ddadang.domain.glucose.score.GlucoseAnalysisProperties;
import com.example.ddadang.domain.glucose.score.GlucoseGroup;
import com.example.ddadang.domain.glucose.status.GlucoseErrorStatus;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.DiabetesType;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.global.config.ClockConfig;
import com.example.ddadang.global.exception.GeneralException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 날짜별 혈당 점수. 확정(freeze)된 날은 저장값을, 오늘/아직 확정 전인 날은 실시간 계산값을 쓴다.
 *
 * <p>확정 규칙: 해당 날짜 다음날 00:00 + freeze.delay가 지나면 확정 저장한다. 단 측정이 0건인 날은
 * 저장하지 않는다(나중에 과거 데이터가 동기화되면 점수가 나올 수 있도록).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyGlucoseScoreService {

    static final int MAX_RANGE_DAYS = 42;
    private static final ZoneId KST = ClockConfig.KST;

    private final DailyGlucoseScoreRepository dailyGlucoseScoreRepository;
    private final CgmReadingRepository cgmReadingRepository;
    private final CgmConnectionRepository cgmConnectionRepository;
    private final MemberRepository memberRepository;
    private final GlucoseAnalysisService glucoseAnalysisService;
    private final GlucoseAnalysisProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    /**
     * 홈 화면용 하루 분석. readings는 그래프용으로 이미 조회한 해당 날짜 측정값을 재사용한다.
     */
    public DailyGlucoseAnalysis getDailyAnalysis(
        Long memberId, DiabetesType diabetesType, LocalDate date, List<CgmReading> readings
    ) {
        return dailyGlucoseScoreRepository.findByMemberIdAndScoreDate(memberId, date)
            .map(DailyGlucoseScore::toAnalysis)
            .orElseGet(() -> analyze(diabetesType, readings, date));
    }

    /**
     * 달력용 기간별 점수. 확정 안 된 날의 측정값은 한 번에 조회해 날짜별로 나눠 계산한다.
     */
    public List<DailyScoreResponse> getDailyScores(Long memberId, LocalDate from, LocalDate to) {
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new GeneralException(GlucoseErrorStatus.INVALID_DATE_RANGE);
        }
        Member member = memberRepository.getReferenceById(memberId);
        Map<LocalDate, DailyGlucoseScore> frozen = dailyGlucoseScoreRepository
            .findByMemberIdAndScoreDateBetween(memberId, from, to).stream()
            .collect(Collectors.toMap(DailyGlucoseScore::getScoreDate, Function.identity()));

        List<LocalDate> liveDates = from.datesUntil(to.plusDays(1)).filter(date -> !frozen.containsKey(date)).toList();
        Map<LocalDate, List<CgmReading>> readingsByDate = liveDates.isEmpty()
            ? Map.of()
            : readingsByDate(memberId, liveDates.get(0), liveDates.get(liveDates.size() - 1));

        List<DailyScoreResponse> responses = new ArrayList<>();
        for (LocalDate date : from.datesUntil(to.plusDays(1)).toList()) {
            DailyGlucoseScore stored = frozen.get(date);
            DailyGlucoseAnalysis analysis = stored != null
                ? stored.toAnalysis()
                : analyze(member.getDiabetesType(), readingsByDate.getOrDefault(date, List.of()), date);
            Double score = analysis.score().score();
            responses.add(new DailyScoreResponse(
                date, score == null ? null : (int) Math.round(score), analysis.score().status(), stored != null
            ));
        }
        return responses;
    }

    /**
     * CGM이 연결된 회원들의 확정 가능한 날짜(최근 lookbackDays일) 중 아직 저장되지 않은 날을 확정 저장한다.
     *
     * @return 새로 확정한 건수
     */
    public int freezeEligibleDays() {
        LocalDate latestEligible = latestFreezableDate();
        LocalDate earliest = latestEligible.minusDays(properties.freeze().lookbackDays() - 1L);
        int frozenCount = 0;
        for (Long memberId : cgmConnectionRepository.findMemberIdsByStatus(CgmConnectionStatus.CONNECTED)) {
            for (LocalDate date : earliest.datesUntil(latestEligible.plusDays(1)).toList()) {
                try {
                    Boolean frozen = transactionTemplate.execute(status -> freeze(memberId, date));
                    if (Boolean.TRUE.equals(frozen)) {
                        frozenCount++;
                    }
                } catch (RuntimeException e) {
                    log.error("혈당 점수 확정 실패 memberId={}, date={}", memberId, date, e);
                }
            }
        }
        return frozenCount;
    }

    /** 다음날 00:00 + delay가 지난 가장 최근 날짜 */
    public LocalDate latestFreezableDate() {
        LocalDateTime threshold = LocalDateTime.now(clock).minus(properties.freeze().delay());
        return threshold.toLocalDate().minusDays(1);
    }

    private boolean freeze(Long memberId, LocalDate date) {
        if (dailyGlucoseScoreRepository.existsByMemberIdAndScoreDate(memberId, date)) {
            return false;
        }
        List<CgmReading> readings = readings(memberId, date, date);
        if (readings.isEmpty()) {
            return false;
        }
        Member member = memberRepository.findById(memberId).orElseThrow();
        DailyGlucoseAnalysis analysis = analyze(member.getDiabetesType(), readings, date);
        dailyGlucoseScoreRepository.save(DailyGlucoseScore.freeze(
            member, date, GlucoseGroup.from(member.getDiabetesType()), analysis, LocalDateTime.now(clock)
        ));
        return true;
    }

    private DailyGlucoseAnalysis analyze(DiabetesType diabetesType, List<CgmReading> readings, LocalDate date) {
        return glucoseAnalysisService.analyze(diabetesType, readings, date, KST, OffsetDateTime.now(clock));
    }

    private Map<LocalDate, List<CgmReading>> readingsByDate(Long memberId, LocalDate from, LocalDate to) {
        return readings(memberId, from, to).stream()
            .collect(Collectors.groupingBy(reading -> reading.getEventAt().atZoneSameInstant(KST).toLocalDate()));
    }

    private List<CgmReading> readings(Long memberId, LocalDate from, LocalDate to) {
        return cgmReadingRepository.findByMemberIdAndEventAtGreaterThanEqualAndEventAtLessThanOrderByEventAtAsc(
            memberId,
            from.atStartOfDay(KST).toOffsetDateTime(),
            to.plusDays(1).atStartOfDay(KST).toOffsetDateTime()
        );
    }
}
