package com.example.ddadang.domain.record.exercise;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ExerciseRecordIntegrationTest extends IntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    @Test
    void 기본_운동_목록이_적재되고_인기_운동과_검색으로_조회된다() throws Exception {
        String auth = bearer(createMember());

        mockMvc.perform(get("/api/exercises").header("Authorization", auth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(exerciseRepository.count()));
        mockMvc.perform(get("/api/exercises").param("popular", "true").header("Authorization", auth))
            .andExpect(jsonPath("$.result[*].popular").value(everyItem(is(true))));
        mockMvc.perform(get("/api/exercises").param("keyword", "걷기").header("Authorization", auth))
            .andExpect(jsonPath("$.result[*].name").value(everyItem(containsString("걷기"))));
    }

    @Test
    void 운동_목록의_30분_소모_열량은_최근_체중_기준이고_체중이_없으면_null() throws Exception {
        Member me = createMember();
        exerciseRepository.save(new Exercise("열량확인용 운동", BigDecimal.valueOf(4.0), false));

        mockMvc.perform(get("/api/exercises").param("keyword", "열량확인용").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].kcalPer30Min").doesNotExist());

        weight(me, "2026-10-01T00:00:00", "60.0");
        weight(me, "2026-10-05T00:00:00", "50.0");
        // 4.0 × 50kg × 0.5h = 100
        mockMvc.perform(get("/api/exercises").param("keyword", "열량확인용").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result[0].kcalPer30Min").value(100.0));

        mockMvc.perform(get("/api/exercises").param("keyword", "가".repeat(21)).header("Authorization", bearer(me)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 최근_기록한_운동은_최신순_중복없이_최대_4개이고_기록을_지우면_빠진다() throws Exception {
        Member me = createMember();
        mockMvc.perform(get("/api/exercises/recent").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(0));

        Exercise a = exercise(3.0);
        Exercise b = exercise(4.0);
        Exercise c = exercise(5.0);
        Exercise d = exercise(6.0);
        Exercise e = exercise(7.0);
        create(me, a, "2026-10-01T08:00:00", 30, null);
        create(me, b, "2026-10-02T08:00:00", 30, null);
        create(me, c, "2026-10-03T08:00:00", 30, null);
        create(me, d, "2026-10-04T08:00:00", 30, null);
        Integer latestA = create(me, a, "2026-10-06T08:00:00", 30, null);
        Integer eRecord = create(me, e, "2026-10-05T08:00:00", 30, null);
        create(createMember(), b, "2026-10-07T08:00:00", 30, null);

        mockMvc.perform(get("/api/exercises/recent").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.length()").value(4))
            .andExpect(jsonPath("$.result[0].exerciseId").value(a.getId()))
            .andExpect(jsonPath("$.result[1].exerciseId").value(e.getId()))
            .andExpect(jsonPath("$.result[2].exerciseId").value(d.getId()))
            .andExpect(jsonPath("$.result[3].exerciseId").value(c.getId()));

        mockMvc.perform(delete("/api/exercise-records/{id}", eRecord).header("Authorization", bearer(me)));
        mockMvc.perform(get("/api/exercises/recent").header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result[1].exerciseId").value(d.getId()))
            .andExpect(jsonPath("$.result[3].exerciseId").value(b.getId()));
        mockMvc.perform(get("/api/exercise-records/{id}", latestA).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
    }

    @Test
    void 운동_일시_이전_가장_최근_체중으로_소모_열량을_계산한다() throws Exception {
        Member me = createMember();
        Exercise exercise = exercise(4.0);
        weight(me, "2026-10-01T07:00:00", "60.0");
        weight(me, "2026-10-05T07:00:00", "50.0");
        weight(me, "2026-10-06T07:00:00", "70.0");

        // 4.0 × 50kg × 30분/60 = 100
        Integer id = create(me, exercise, "2026-10-05T19:00:00", 30, "저녁 산책");
        mockMvc.perform(get("/api/exercise-records/{id}", id).header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.kcal").value(100.0))
            .andExpect(jsonPath("$.result.durationMin").value(30))
            .andExpect(jsonPath("$.result.memo").value("저녁 산책"));

        // 일시를 바꾸면 그 시점 체중으로 다시 계산: 4.0 × 60kg × 45분/60 = 180
        mockMvc.perform(put("/api/exercise-records/{id}", id)
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(exercise, "2026-10-02T19:00:00", 45, null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.kcal").value(180.0))
            .andExpect(jsonPath("$.result.memo").doesNotExist());
    }

    @Test
    void 이전_체중이_없으면_가장_오래된_체중을_쓰고_체중_기록이_없으면_열량은_null() throws Exception {
        Member noWeight = createMember();
        Exercise exercise = exercise(4.0);
        Integer withoutWeight = create(noWeight, exercise, "2026-10-05T19:00:00", 30, null);
        mockMvc.perform(get("/api/exercise-records/{id}", withoutWeight).header("Authorization", bearer(noWeight)))
            .andExpect(jsonPath("$.result.kcal").doesNotExist());

        Member me = createMember();
        weight(me, "2026-10-10T07:00:00", "55.0");
        weight(me, "2026-10-20T07:00:00", "65.0");
        Integer beforeFirstWeight = create(me, exercise, "2026-10-05T19:00:00", 60, null);
        mockMvc.perform(get("/api/exercise-records/{id}", beforeFirstWeight).header("Authorization", bearer(me)))
            .andExpect(jsonPath("$.result.kcal").value(220.0));
    }

    @Test
    void 운동_기록을_날짜별로_시간순_조회하고_삭제한다() throws Exception {
        Member me = createMember();
        Exercise exercise = exercise(3.5);
        create(me, exercise, "2026-10-05T19:00:00", 30, null);
        Integer morning = create(me, exercise, "2026-10-05T07:00:00", 20, null);
        create(me, exercise, "2026-10-06T07:00:00", 20, null);

        mockMvc.perform(get("/api/exercise-records").param("date", "2026-10-05").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].exerciseRecordId").value(morning))
            .andExpect(jsonPath("$.result[0].name").value("테스트 운동"));

        mockMvc.perform(delete("/api/exercise-records/{id}", morning).header("Authorization", bearer(me)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/exercise-records/{id}", morning).header("Authorization", bearer(me)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EXERCISE_4041"));
    }

    @Test
    void 없는_운동이거나_다른_회원의_기록이면_404() throws Exception {
        Member me = createMember();
        mockMvc.perform(post("/api/exercise-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseId\":999999,\"performedAt\":\"2026-10-05T19:00:00\",\"durationMin\":30}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EXERCISE_4040"));

        Exercise exercise = exercise(3.5);
        Integer id = create(me, exercise, "2026-10-05T19:00:00", 30, null);
        String other = bearer(createMember());
        mockMvc.perform(get("/api/exercise-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/exercise-records/{id}", id)
                .header("Authorization", other)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(exercise, "2026-10-05T19:00:00", 10, null)))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/exercise-records/{id}", id).header("Authorization", other))
            .andExpect(status().isNotFound());
    }

    @Test
    void 운동_시간이_범위를_벗어나면_400() throws Exception {
        Member me = createMember();
        Exercise exercise = exercise(3.5);

        for (int duration : new int[] {0, 1441}) {
            mockMvc.perform(post("/api/exercise-records")
                    .header("Authorization", bearer(me))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(exercise, "2026-10-05T19:00:00", duration, null)))
                .andExpect(status().isBadRequest());
        }
    }

    private Exercise exercise(double met) {
        return exerciseRepository.save(new Exercise("테스트 운동", BigDecimal.valueOf(met), false));
    }

    private void weight(Member member, String measuredAt, String weightKg) {
        weightRecordRepository.save(new WeightRecord(member, LocalDateTime.parse(measuredAt), new BigDecimal(weightKg)));
    }

    private Integer create(Member member, Exercise exercise, String performedAt, int durationMin, String memo)
        throws Exception {
        String body = mockMvc.perform(post("/api/exercise-records")
                .header("Authorization", bearer(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(exercise, performedAt, durationMin, memo)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.result.exerciseRecordId");
    }

    private String json(Exercise exercise, String performedAt, int durationMin, String memo) {
        String memoJson = memo == null ? "null" : "\"" + memo + "\"";
        return "{\"exerciseId\":%d,\"performedAt\":\"%s\",\"durationMin\":%d,\"memo\":%s}"
            .formatted(exercise.getId(), performedAt, durationMin, memoJson);
    }
}
