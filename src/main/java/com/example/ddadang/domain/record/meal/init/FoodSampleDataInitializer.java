package com.example.ddadang.domain.record.meal.init;

import static com.example.ddadang.domain.record.meal.enums.FoodCategory.GENERAL;
import static com.example.ddadang.domain.record.meal.enums.FoodCategory.PROCESSED;
import static com.example.ddadang.domain.record.meal.enums.FoodUnit.G;
import static com.example.ddadang.domain.record.meal.enums.FoodUnit.ML;

import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.Nutrients;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import com.example.ddadang.domain.record.meal.repository.FoodRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 식약처 식품영양성분 DB 적재 전까지 음식 검색 테스트용 샘플 데이터를 넣는다.
 * 영양 수치는 1회 제공량 기준 근사값이다. DB 음식이 하나라도 있으면 아무것도 하지 않는다.
 * TODO: 실제 음식 DB 적재 후 제거.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "food.sample-data.enabled", havingValue = "true")
public class FoodSampleDataInitializer implements ApplicationRunner {

    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (foodRepository.existsBySource(FoodSource.DB)) {
            return;
        }
        List<Food> samples = List.of(
            // 이름, 브랜드, 분류, 양, 단위, kcal, 탄수, 당류, 식이섬유, 단백질, 지방, 나트륨, 카페인
            food("흰쌀밥", null, GENERAL, 210, G, 315, 69.7, 0.1, 0.6, 5.5, 0.6, 4, 0),
            food("흑미밥", null, GENERAL, 210, G, 320, 69.0, 0.2, 2.1, 6.5, 1.4, 4, 0),
            food("잡곡밥", null, GENERAL, 210, G, 318, 68.0, 0.3, 3.0, 7.0, 1.5, 5, 0),
            food("현미밥", null, GENERAL, 210, G, 330, 71.0, 0.3, 3.0, 6.9, 2.2, 5, 0),
            food("햇반", "CJ제일제당", PROCESSED, 210, G, 315, 70.0, 0.0, 0.5, 6.0, 1.0, 10, 0),
            food("김치찌개", null, GENERAL, 400, G, 200, 10.0, 3.0, 3.0, 15.0, 11.0, 1800, 0),
            food("참치김치찌개", null, GENERAL, 400, G, 250, 11.0, 3.5, 3.0, 20.0, 14.0, 1900, 0),
            food("된장찌개", null, GENERAL, 400, G, 180, 14.0, 3.0, 4.0, 13.0, 7.0, 1700, 0),
            food("미역국", null, GENERAL, 300, G, 90, 4.0, 1.0, 1.5, 7.0, 5.0, 900, 0),
            food("어묵국", null, GENERAL, 300, G, 120, 12.0, 2.0, 0.5, 8.0, 4.0, 1100, 0),
            food("계란말이", null, GENERAL, 100, G, 180, 3.0, 1.5, 0.2, 12.0, 13.0, 350, 0),
            food("콩나물무침", null, GENERAL, 70, G, 35, 3.0, 1.0, 1.5, 3.0, 2.0, 250, 0),
            food("배추김치", null, GENERAL, 50, G, 15, 2.5, 1.0, 1.2, 1.0, 0.3, 450, 0),
            food("떡볶이", null, GENERAL, 300, G, 480, 95.0, 18.0, 2.0, 10.0, 6.0, 1400, 0),
            food("군만두", null, GENERAL, 150, G, 420, 40.0, 2.0, 2.0, 13.0, 23.0, 700, 0),
            food("햄야채볶음밥", null, GENERAL, 350, G, 600, 85.0, 3.0, 2.5, 16.0, 21.0, 1300, 0),
            food("짜장면", null, GENERAL, 650, G, 860, 130.0, 12.0, 6.0, 25.0, 25.0, 2400, 0),
            food("비빔밥", null, GENERAL, 450, G, 600, 95.0, 8.0, 6.0, 20.0, 15.0, 1200, 0),
            food("김밥", null, GENERAL, 250, G, 480, 75.0, 4.0, 3.0, 13.0, 13.0, 1100, 0),
            food("불고기", null, GENERAL, 150, G, 300, 12.0, 9.0, 0.8, 25.0, 17.0, 700, 0),
            food("제육볶음", null, GENERAL, 150, G, 350, 13.0, 8.0, 1.5, 22.0, 23.0, 900, 0),
            food("삶은 달걀", null, GENERAL, 50, G, 75, 0.6, 0.4, 0.0, 6.3, 5.0, 60, 0),
            food("두부", null, GENERAL, 100, G, 84, 2.0, 0.5, 0.5, 9.0, 5.0, 5, 0),
            food("샐러드(드레싱 제외)", null, GENERAL, 150, G, 30, 6.0, 3.0, 2.5, 2.0, 0.3, 20, 0),
            food("토마토샌드위치", null, GENERAL, 200, G, 400, 45.0, 6.0, 3.0, 14.0, 18.0, 800, 0),
            food("베이글", null, GENERAL, 100, G, 260, 51.0, 6.0, 2.2, 10.0, 1.6, 450, 0),
            food("고구마", null, GENERAL, 150, G, 190, 45.0, 9.0, 4.0, 2.0, 0.2, 20, 0),
            food("오트밀", null, GENERAL, 40, G, 150, 27.0, 0.4, 4.0, 5.0, 3.0, 2, 0),
            food("바나나", null, GENERAL, 100, G, 93, 22.0, 12.0, 2.0, 1.1, 0.3, 1, 0),
            food("사과", null, GENERAL, 200, G, 106, 28.0, 22.0, 3.0, 0.4, 0.3, 2, 0),
            food("신라면", "농심", PROCESSED, 120, G, 500, 79.0, 4.0, 3.0, 10.0, 16.0, 1790, 0),
            food("닭가슴살", "하림", PROCESSED, 100, G, 110, 1.0, 0.5, 0.0, 23.0, 1.5, 400, 0),
            food("참치", "동원", PROCESSED, 100, G, 190, 0.0, 0.0, 0.0, 20.0, 12.0, 400, 0),
            food("흰우유", "서울우유", PROCESSED, 200, ML, 130, 10.0, 10.0, 0.0, 6.0, 7.0, 100, 0),
            food("아메리카노", null, GENERAL, 355, ML, 10, 1.5, 0.0, 0.0, 0.7, 0.2, 10, 150)
        );
        foodRepository.saveAll(samples);
        log.info("샘플 음식 데이터 {}건을 적재했습니다.", samples.size());
    }

    private Food food(
        String name, String brand, FoodCategory category, double amount, FoodUnit unit,
        double kcal, double carb, double sugars, double fiber, double protein, double fat, double sodium, double caffeine
    ) {
        Nutrients nutrients = Nutrients.builder()
            .kcal(decimal(kcal))
            .carbohydrateG(decimal(carb))
            .sugarsG(decimal(sugars))
            .dietaryFiberG(decimal(fiber))
            .proteinG(decimal(protein))
            .fatG(decimal(fat))
            .sodiumMg(decimal(sodium))
            .caffeineMg(caffeine == 0 ? null : decimal(caffeine))
            .build();
        return Food.dbFood(name, brand, category, decimal(amount), unit, nutrients);
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value);
    }
}
