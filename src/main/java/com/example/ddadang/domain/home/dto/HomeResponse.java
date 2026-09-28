package com.example.ddadang.domain.home.dto;

import com.example.ddadang.domain.glucose.enums.CgmProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public record HomeResponse(
    LocalDate date,
    Sensor sensor,
    @Schema(description = "회원 목표 혈당 범위(온보딩 입력값). 미입력이면 null") TargetRange targetRange,
    Graph graph,
    Summary summary,
    @Schema(description = "그래프 위 식사 표시용 식사 기록") List<Meal> meals
) {

    public record Sensor(
        boolean connected,
        CgmProvider provider,
        @Schema(description = "센서 부착 후 N일차(부착일=1). 센서 정보가 없으면 null") Integer sensorDay
    ) {

        public static Sensor disconnected() {
            return new Sensor(false, null, null);
        }
    }

    public record TargetRange(Integer min, Integer max) {
    }

    public record Graph(
        @Schema(description = "CGM 혈당 포인트(시간순)") List<Point> points,
        @Schema(description = "하루 최저 혈당(mg/dL). 데이터 없으면 null") Integer minGlucose,
        @Schema(description = "하루 최고 혈당(mg/dL). 데이터 없으면 null") Integer maxGlucose
    ) {
    }

    public record Point(
        OffsetDateTime time,
        Integer value,
        @Schema(description = "추세 0=Unknown, 1=빠르게 감소 ~ 4=안정 ~ 7=빠르게 증가") Integer trend
    ) {
    }

    public record Summary(
        @Schema(description = "혈당 점수(0~100). 계산 기준 확정 전까지 null") Integer glucoseScore,
        @Schema(description = "최고 혈당(mg/dL)") Integer maxGlucose,
        @Schema(description = "평균 혈당(mg/dL)") Integer averageGlucose,
        @Schema(description = "스파이크 횟수. 계산 기준 확정 전까지 null") Integer spikeCount,
        @Schema(description = "식사 기록 탄수화물 합계(g)") BigDecimal carbohydrateG
    ) {
    }

    public record Meal(
        Long mealRecordId,
        LocalDateTime eatenAt,
        @Schema(description = "대표 음식명(첫 번째 메뉴)") String representativeFoodName,
        int itemCount,
        BigDecimal kcal,
        BigDecimal carbohydrateG
    ) {
    }
}
