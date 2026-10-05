package com.example.ddadang.domain.record.exercise.init;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운동 기본 목록을 적재한다. MET는 Compendium of Physical Activities 기준 근사값.
 * 운동이 하나라도 있으면 아무것도 하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseDataInitializer implements ApplicationRunner {

    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (exerciseRepository.count() > 0) {
            return;
        }
        List<Exercise> exercises = List.of(
            // 이름, MET, 인기 운동 여부
            exercise("걷기", 3.5, true),
            exercise("빠르게 걷기", 4.3, true),
            exercise("달리기", 8.0, true),
            exercise("자전거 타기", 6.8, true),
            exercise("수영", 6.0, true),
            exercise("등산", 6.0, true),
            exercise("요가", 2.5, true),
            exercise("필라테스", 3.0, true),
            exercise("근력 운동", 5.0, true),
            exercise("실내 자전거", 6.8, false),
            exercise("러닝머신 걷기", 3.5, false),
            exercise("스트레칭", 2.3, false),
            exercise("계단 오르기", 4.0, false),
            exercise("줄넘기", 11.8, false),
            exercise("에어로빅", 7.3, false),
            exercise("댄스", 5.0, false),
            exercise("배드민턴", 5.5, false),
            exercise("테니스", 7.3, false),
            exercise("탁구", 4.0, false),
            exercise("골프", 4.8, false),
            exercise("축구", 7.0, false),
            exercise("농구", 6.5, false)
        );
        exerciseRepository.saveAll(exercises);
        log.info("운동 기본 데이터 {}건을 적재했습니다.", exercises.size());
    }

    private Exercise exercise(String name, double met, boolean popular) {
        return new Exercise(name, BigDecimal.valueOf(met), popular);
    }
}
