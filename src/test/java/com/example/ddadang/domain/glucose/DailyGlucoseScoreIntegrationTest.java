package com.example.ddadang.domain.glucose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.glucose.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.repository.CgmReadingRepository;
import com.example.ddadang.domain.glucose.repository.DailyGlucoseScoreRepository;
import com.example.ddadang.domain.glucose.score.GlucoseScoreStatus;
import com.example.ddadang.domain.glucose.service.DailyGlucoseScoreService;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.support.FixedClockConfig;
import com.example.ddadang.support.IntegrationTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * 현재 시각 2026-09-29 10:00 KST, 확정 유예 0(자정) → 9/28까지 확정 가능.
 * 테스트 회원은 당뇨 유형 미입력(Non-DM)이라 혈당 100으로 유지하면 감점 없이 100점.
 */
@Import(FixedClockConfig.class)
class DailyGlucoseScoreIntegrationTest extends IntegrationTest {

    private static final LocalDate D26 = LocalDate.of(2026, 9, 26);
    private static final LocalDate D27 = LocalDate.of(2026, 9, 27);
    private static final LocalDate D28 = LocalDate.of(2026, 9, 28);

    @Autowired
    private DailyGlucoseScoreService dailyGlucoseScoreService;

    @Autowired
    private DailyGlucoseScoreRepository dailyGlucoseScoreRepository;

    @Autowired
    private CgmConnectionRepository cgmConnectionRepository;

    @Autowired
    private CgmReadingRepository cgmReadingRepository;

    private long seq = 1;

    @Test
    void 지난_날짜를_확정_저장하고_측정_0건인_날은_저장하지_않는다() {
        Member member = createMember();
        CgmConnection connection = connect(member);
        fillDay(connection, D28, 0, 1440, 100);   // 하루 전체 → 100점
        fillDay(connection, D27, 0, 60, 100);     // 1시간 → 커버리지 미달

        dailyGlucoseScoreService.freezeEligibleDays();

        var d28 = dailyGlucoseScoreRepository.findByMemberIdAndScoreDate(member.getId(), D28).orElseThrow();
        assertThat(d28.getStatus()).isEqualTo(GlucoseScoreStatus.FINAL);
        assertThat(d28.getScore()).isEqualTo(100.0);
        var d27 = dailyGlucoseScoreRepository.findByMemberIdAndScoreDate(member.getId(), D27).orElseThrow();
        assertThat(d27.getStatus()).isEqualTo(GlucoseScoreStatus.UNAVAILABLE);
        assertThat(dailyGlucoseScoreRepository.existsByMemberIdAndScoreDate(member.getId(), D26)).isFalse();

        // 다시 실행해도 이미 확정된 날은 건드리지 않는다
        long before = dailyGlucoseScoreRepository.count();
        dailyGlucoseScoreService.freezeEligibleDays();
        assertThat(dailyGlucoseScoreRepository.count()).isEqualTo(before);
    }

    @Test
    void 확정_후_늦게_들어온_데이터는_점수에_반영되지_않고_측정없던_날은_나중에_계산된다() throws Exception {
        Member member = createMember();
        CgmConnection connection = connect(member);
        fillDay(connection, D27, 0, 60, 100);
        dailyGlucoseScoreService.freezeEligibleDays();

        // 늦게 동기화된 데이터: 9/27 나머지, 9/26 하루 전체
        fillDay(connection, D27, 60, 1440, 100);
        fillDay(connection, D26, 0, 1440, 100);

        mockMvc.perform(get("/api/glucose/daily-scores")
                .param("from", "2026-09-26").param("to", "2026-09-30")
                .header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(5))
            // 9/26: 확정 전(측정 0건이라 저장 안 됨) → 늦은 데이터로 실시간 계산
            .andExpect(jsonPath("$.result[0].status").value("FINAL"))
            .andExpect(jsonPath("$.result[0].score").value(100))
            .andExpect(jsonPath("$.result[0].frozen").value(false))
            // 9/27: 미산출로 확정됨 → 늦은 데이터 무시
            .andExpect(jsonPath("$.result[1].status").value("UNAVAILABLE"))
            .andExpect(jsonPath("$.result[1].frozen").value(true))
            // 9/29: 오늘, 측정 없음 → 미산출 / 9/30: 미래
            .andExpect(jsonPath("$.result[3].date").value("2026-09-29"))
            .andExpect(jsonPath("$.result[4].status").value("UNAVAILABLE"));

        mockMvc.perform(get("/api/home").param("date", "2026-09-27").header("Authorization", bearer(member)))
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("UNAVAILABLE"));
    }

    @Test
    void 오늘은_잠정_점수로_계산한다() throws Exception {
        Member member = createMember();
        CgmConnection connection = connect(member);
        fillDay(connection, LocalDate.of(2026, 9, 29), 0, 600, 100);

        mockMvc.perform(get("/api/glucose/daily-scores")
                .param("from", "2026-09-29").param("to", "2026-09-29")
                .header("Authorization", bearer(member)))
            .andExpect(jsonPath("$.result[0].status").value("PROVISIONAL"))
            .andExpect(jsonPath("$.result[0].score").value(100))
            .andExpect(jsonPath("$.result[0].frozen").value(false));
    }

    @Test
    void 조회_기간이_잘못되면_400() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/api/glucose/daily-scores")
                .param("from", "2026-09-30").param("to", "2026-09-01")
                .header("Authorization", bearer(member)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("GLUCOSE_4000"));
        mockMvc.perform(get("/api/glucose/daily-scores")
                .param("from", "2026-08-01").param("to", "2026-09-30")
                .header("Authorization", bearer(member)))
            .andExpect(status().isBadRequest());
    }

    private CgmConnection connect(Member member) {
        return cgmConnectionRepository.save(CgmConnection.connect(
            member, CgmProvider.CARESENS_AIR, UUID.randomUUID().toString(),
            "access", "refresh", LocalDateTime.now().plusHours(1)
        ));
    }

    private void fillDay(CgmConnection connection, LocalDate date, int fromMinute, int toMinute, double value) {
        OffsetDateTime start = date.atStartOfDay(java.time.ZoneId.of("Asia/Seoul")).toOffsetDateTime();
        for (int minute = fromMinute; minute < toMinute; minute += 5) {
            cgmReadingRepository.save(CgmReading.of(connection, new CgmSampleResponse(
                "SN-1", seq++, start.plusMinutes(minute), 540, 2, value, value, 0.0, 4, 0, 0
            )));
        }
    }
}
