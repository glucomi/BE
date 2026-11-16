package com.example.ddadang.domain.record.exercise.init;

import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 시작 시 운동 DB(data/exercise.csv)를 exercise 테이블에 맞춘다.
 * 원본코드 기준으로 없으면 추가하고 있으면 CSV 값으로 갱신하므로 여러 번 실행해도 결과가 같다.
 * 운동 DB 이전의 임시 운동(원본코드 없음)은 지우되, 운동 기록에서 쓰고 있으면 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseDataInitializer implements ApplicationRunner {

    /**
     * 인기 운동(원본코드). 정책 미확정이라 임시 지정:
     * 걷기(보통), 걷기(빠르게), 달리기(보통), 자전거(보통), 수영(일반), 하이킹, 요가(하타), 필라테스, 웨이트 트레이닝.
     */
    private static final Set<String> POPULAR_CODES = Set.of(
        "17190", "17200", "12050", "01014", "18310", "17080", "02150", "02105", "02050"
    );

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCsvReader exerciseCsvReader;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Exercise> byCode = exerciseRepository.findByCodeIsNotNull().stream()
            .collect(Collectors.toMap(Exercise::getCode, Function.identity()));

        int added = 0;
        for (ExerciseCsvReader.Row row : exerciseCsvReader.read()) {
            boolean popular = POPULAR_CODES.contains(row.code());
            Exercise exercise = byCode.get(row.code());
            if (exercise == null) {
                exerciseRepository.save(new Exercise(
                    row.code(), row.category(), row.name(), row.nameEn(), row.nameJa(), row.met(), popular
                ));
                added++;
            } else {
                exercise.update(row.category(), row.name(), row.nameEn(), row.nameJa(), row.met(), popular);
            }
        }

        List<Exercise> unused = exerciseRepository.findUnusedWithoutCode();
        exerciseRepository.deleteAll(unused);

        if (added > 0 || !unused.isEmpty()) {
            log.info("운동 DB 적재: {}건 추가, 원본코드 없는 미사용 운동 {}건 삭제", added, unused.size());
        }
    }
}
