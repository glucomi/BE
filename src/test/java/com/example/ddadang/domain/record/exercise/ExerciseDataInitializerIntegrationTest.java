package com.example.ddadang.domain.record.exercise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.init.ExerciseDataInitializer;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.support.IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ExerciseDataInitializerIntegrationTest extends IntegrationTest {

    private static final int EXERCISE_DB_SIZE = 159;

    @Autowired
    private ExerciseDataInitializer exerciseDataInitializer;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    @Test
    void 운동_DB_CSV가_원본코드_분류_다국어명과_함께_적재된다() {
        List<Exercise> exercises = exerciseRepository.findByCodeIsNotNull();

        assertThat(exercises).hasSize(EXERCISE_DB_SIZE);
        assertThat(exercises).extracting(Exercise::getCategory).doesNotContainNull().doesNotContain("");
        assertThat(exercises.stream().map(Exercise::getCategory).distinct()).hasSize(14);
        assertThat(exercises).filteredOn(Exercise::isPopular).hasSize(9);

        // 앞자리 0이 있는 코드, 쉼표가 들어간 운동명·영문명(따옴표로 감싼 값)
        Exercise crunch = byCode(exercises, "02024");
        assertThat(crunch.getCategory()).isEqualTo("근력/웨이트");
        assertThat(crunch.getName()).isEqualTo("맨몸운동(크런치·플랭크, 가벼움)");
        assertThat(crunch.getMet()).isEqualByComparingTo("2.8");
        assertThat(crunch.getNameEn()).isEqualTo("Calisthenics (e.g., curl ups, abdominal crunches, plank), light effort");
        assertThat(crunch.getNameJa()).isEqualTo("健康体操(例：カールアップ、アブドミナルクランチ、プランク)：楽な労力");

        Exercise marathon = byCode(exercises, "12200");
        assertThat(marathon.getName()).isEqualTo("마라톤");
        assertThat(marathon.getMet()).isEqualByComparingTo("13.3");
    }

    @Test
    void 운동_목록_응답에_분류와_원본코드가_포함되고_열량은_MET_체중_시간으로_계산된다() throws Exception {
        Member me = createMember();
        weightRecordRepository.save(new WeightRecord(me, LocalDateTime.parse("2026-10-05T00:00:00"), new BigDecimal("60.0")));

        // 케틀벨 스윙 MET 9.8 × 60kg × (30 ÷ 60) = 294 (CSV의 "30분 기준 소모(60kg)"와 같은 값)
        mockMvc.perform(get("/api/exercises").param("keyword", "케틀벨 스윙").header("Authorization", bearer(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].code").value("02058"))
            .andExpect(jsonPath("$.result[0].category").value("근력/웨이트"))
            .andExpect(jsonPath("$.result[0].nameEn").value("Kettle bell swings"))
            .andExpect(jsonPath("$.result[0].nameJa").value("ケトルベルスウィング"))
            .andExpect(jsonPath("$.result[0].met").value(9.8))
            .andExpect(jsonPath("$.result[0].kcalPer30Min").value(294.0));

        // 45분: 9.8 × 60 × (45 ÷ 60) = 441
        Long kettlebellId = byCode(exerciseRepository.findByCodeIsNotNull(), "02058").getId();
        mockMvc.perform(post("/api/exercise-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseId\":%d,\"performedAt\":\"2026-10-05T19:00:00\",\"durationMin\":45}"
                    .formatted(kettlebellId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.kcal").value(441.0));
    }

    @Test
    void 다시_실행하면_CSV값으로_되돌리고_원본코드_없는_운동은_기록에_쓰인_것만_남긴다() throws Exception {
        Member me = createMember();
        Exercise unusedLegacy = exerciseRepository.save(new Exercise("임시 운동(미사용)", BigDecimal.valueOf(3.0), true));
        Exercise usedLegacy = exerciseRepository.save(new Exercise("임시 운동(기록 있음)", BigDecimal.valueOf(3.0), true));
        mockMvc.perform(post("/api/exercise-records")
                .header("Authorization", bearer(me))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseId\":%d,\"performedAt\":\"2026-10-05T19:00:00\",\"durationMin\":30}"
                    .formatted(usedLegacy.getId())))
            .andExpect(status().isCreated());

        Exercise marathon = byCode(exerciseRepository.findByCodeIsNotNull(), "12200");
        marathon.update("바뀐 분류", "바뀐 이름", null, null, BigDecimal.valueOf(1.0), true);
        exerciseRepository.save(marathon);

        exerciseDataInitializer.run(null);

        assertThat(exerciseRepository.findByCodeIsNotNull()).hasSize(EXERCISE_DB_SIZE);
        Exercise restored = exerciseRepository.findById(marathon.getId()).orElseThrow();
        assertThat(restored.getName()).isEqualTo("마라톤");
        assertThat(restored.getCategory()).isEqualTo("걷기/러닝");
        assertThat(restored.getMet()).isEqualByComparingTo("13.3");
        assertThat(restored.isPopular()).isFalse();
        assertThat(exerciseRepository.findById(unusedLegacy.getId())).isEmpty();
        assertThat(exerciseRepository.findById(usedLegacy.getId())).isPresent();
    }

    private Exercise byCode(List<Exercise> exercises, String code) {
        return exercises.stream().filter(exercise -> code.equals(exercise.getCode())).findFirst().orElseThrow();
    }
}
