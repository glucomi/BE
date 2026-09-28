package com.example.ddadang.domain.record.meal.entity;

import com.example.ddadang.domain.record.meal.enums.IntakeUnit;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 식사 기록의 메뉴 1건. 영양 성분은 기록 시점의 섭취량 기준으로 환산해 스냅샷으로 저장하므로,
 * 이후 음식 DB 값이 바뀌어도 과거 기록은 변하지 않는다.
 */
@Entity
@Table(name = "meal_record_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealRecordItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecord mealRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 10)
    private IntakeUnit unit;

    @Embedded
    private Nutrients nutrients;

    public static MealRecordItem of(MealRecord mealRecord, Food food, BigDecimal amount, IntakeUnit unit) {
        MealRecordItem item = new MealRecordItem();
        item.mealRecord = mealRecord;
        item.food = food;
        item.amount = amount;
        item.unit = unit;
        item.nutrients = food.getNutrients().scale(intakeRatio(food, amount, unit));
        return item;
    }

    private static BigDecimal intakeRatio(Food food, BigDecimal amount, IntakeUnit unit) {
        if (unit == IntakeUnit.SERVING) {
            return amount;
        }
        return amount.divide(food.getServingAmount(), 6, RoundingMode.HALF_UP);
    }
}
