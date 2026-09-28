package com.example.ddadang.domain.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.glucose.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.repository.CgmReadingRepository;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.DiabetesType;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.Nutrients;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import com.example.ddadang.domain.record.meal.repository.FoodRepository;
import com.example.ddadang.support.IntegrationTest;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

class HomeIntegrationTest extends IntegrationTest {

    /** 현재 시각 고정: 2026-09-29 10:00 KST → 9/28은 지난 날(확정), 9/29는 오늘(잠정) */
    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-09-29T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        }
    }

    @Autowired
    private CgmConnectionRepository cgmConnectionRepository;

    @Autowired
    private CgmReadingRepository cgmReadingRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Test
    void 선택한_날짜의_혈당_그래프와_요약지표_식사표시를_조회한다() throws Exception {
        Member member = onboardedMember();
        CgmConnection connection = connect(member);

        // 센서 부착 9/26 → 9/28은 3일차
        reading(connection, 1, "2026-09-26T08:00:00+09:00", 100.0);
        connection.updateSensor("SN-1", OffsetDateTime.parse("2026-09-26T08:00:00+09:00"));
        cgmConnectionRepository.save(connection);

        // 9/28 KST 하루 구간: 경계 밖(9/27 23:55, 9/29 00:00)은 제외
        reading(connection, 2, "2026-09-27T23:55:00+09:00", 300.0);
        reading(connection, 3, "2026-09-28T00:00:00+09:00", 90.0);
        reading(connection, 4, "2026-09-28T12:00:00+09:00", 180.4);
        reading(connection, 5, "2026-09-28T13:00:00+09:00", 120.0);
        reading(connection, 6, "2026-09-29T00:00:00+09:00", 50.0);

        Food rice = foodRepository.save(Food.dbFood(
            "흑미밥", null, FoodCategory.GENERAL, new BigDecimal("210"), FoodUnit.G,
            Nutrients.builder().kcal(new BigDecimal("320")).carbohydrateG(new BigDecimal("69")).build()
        ));
        recordMeal(member, "2026-09-28T11:30:00", rice.getId());
        recordMeal(member, "2026-09-29T08:00:00", rice.getId());

        mockMvc.perform(get("/api/home").param("date", "2026-09-28").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.date").value("2026-09-28"))
            .andExpect(jsonPath("$.result.sensor.connected").value(true))
            .andExpect(jsonPath("$.result.sensor.provider").value("CARESENS_AIR"))
            .andExpect(jsonPath("$.result.sensor.sensorDay").value(3))
            .andExpect(jsonPath("$.result.targetRange.min").value(70))
            .andExpect(jsonPath("$.result.targetRange.max").value(140))
            .andExpect(jsonPath("$.result.graph.points.length()").value(3))
            .andExpect(jsonPath("$.result.graph.points[1].value").value(180))
            .andExpect(jsonPath("$.result.graph.minGlucose").value(90))
            .andExpect(jsonPath("$.result.graph.maxGlucose").value(180))
            .andExpect(jsonPath("$.result.summary.maxGlucose").value(180))
            // 측정 3건뿐이라 커버리지 70% 미만 → 평균/점수 미산출
            .andExpect(jsonPath("$.result.summary.averageGlucose").doesNotExist())
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("UNAVAILABLE"))
            .andExpect(jsonPath("$.result.summary.spikeCount").value(0))
            .andExpect(jsonPath("$.result.summary.carbohydrateG").value(69.0))
            .andExpect(jsonPath("$.result.meals.length()").value(1))
            .andExpect(jsonPath("$.result.meals[0].representativeFoodName").value("흑미밥"));
    }

    @Test
    void 하루치_데이터가_있으면_확정_점수를_오늘은_잠정_점수를_계산한다() throws Exception {
        Member member = onboardedMember();
        CgmConnection connection = connect(member);
        OffsetDateTime dayStart = OffsetDateTime.parse("2026-09-28T00:00:00+09:00");
        long seq = 1;
        // 9/28 하루 종일 5분 간격 110, 12:00~13:00에 급상승 스파이크(최대 200, 20분 유지)
        // 평균 = 110 + 추가면적 3375 / 측정 1435분 = 112.35 → 112
        for (int minute = 0; minute < 1440; minute += 5) {
            int t = minute - 720;
            double value = t < 0 || t > 50 ? 110 : (t <= 30 ? 110 + t * 3.0 : 200);
            reading(connection, seq++, dayStart.plusMinutes(minute).toString(), value);
        }
        // 9/29 00:00~10:00(현재) 5분 간격 110
        for (int minute = 0; minute < 600; minute += 5) {
            reading(connection, seq++, dayStart.plusDays(1).plusMinutes(minute).toString(), 110);
        }

        mockMvc.perform(get("/api/home").param("date", "2026-09-28").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("FINAL"))
            .andExpect(jsonPath("$.result.summary.spikeCount").value(1))
            .andExpect(jsonPath("$.result.summary.glucoseScoreDeductions.spike").value(3.0))
            .andExpect(jsonPath("$.result.summary.glucoseScore").value(97))
            .andExpect(jsonPath("$.result.summary.averageGlucose").value(112));

        mockMvc.perform(get("/api/home").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.date").value("2026-09-29"))
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("PROVISIONAL"))
            .andExpect(jsonPath("$.result.summary.glucoseScore").value(100))
            .andExpect(jsonPath("$.result.summary.averageGlucose").value(110));

        mockMvc.perform(get("/api/home").param("date", "2026-09-30").header("Authorization", bearer(member)))
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("UNAVAILABLE"))
            .andExpect(jsonPath("$.result.summary.spikeCount").doesNotExist());
    }

    @Test
    void 센서_미연결이고_데이터가_없으면_빈_그래프를_내려준다() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/api/home").param("date", "2026-09-28").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.sensor.connected").value(false))
            .andExpect(jsonPath("$.result.sensor.sensorDay").doesNotExist())
            .andExpect(jsonPath("$.result.targetRange").doesNotExist())
            .andExpect(jsonPath("$.result.graph.points.length()").value(0))
            .andExpect(jsonPath("$.result.summary.averageGlucose").doesNotExist())
            .andExpect(jsonPath("$.result.summary.carbohydrateG").value(0))
            .andExpect(jsonPath("$.result.meals.length()").value(0));
    }

    @Test
    void 날짜를_비우면_오늘_기준으로_조회한다() throws Exception {
        Member member = createMember();

        mockMvc.perform(get("/api/home").header("Authorization", bearer(member)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.date").value("2026-09-29"))
            .andExpect(jsonPath("$.result.summary.glucoseScoreStatus").value("UNAVAILABLE"));
    }

    private Member onboardedMember() {
        Member member = createMember();
        member.completeSignup(false);
        member.completeOnboarding(DiabetesType.TYPE1, new BigDecimal("170"), 70, 140);
        return memberRepository.save(member);
    }

    private CgmConnection connect(Member member) {
        return cgmConnectionRepository.save(CgmConnection.connect(
            member, CgmProvider.CARESENS_AIR, UUID.randomUUID().toString(),
            "access", "refresh", LocalDateTime.now().plusHours(1)
        ));
    }

    private void reading(CgmConnection connection, long seq, String eventAt, double value) {
        cgmReadingRepository.save(CgmReading.of(connection, new CgmSampleResponse(
            "SN-1", seq, OffsetDateTime.parse(eventAt), 540, 2, value, value, 0.0, 4, 0, 0
        )));
    }

    private void recordMeal(Member member, String eatenAt, Long foodId) throws Exception {
        mockMvc.perform(post("/api/meal-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"eatenAt":"%s","items":[{"foodId":%d,"amount":1,"unit":"SERVING"}]}
                    """.formatted(eatenAt, foodId)))
            .andExpect(status().isCreated());
    }
}
