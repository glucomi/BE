package com.example.ddadang.domain.record.meal.entity;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.meal.enums.FoodCategory;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import com.example.ddadang.domain.record.meal.enums.FoodUnit;
import com.example.ddadang.domain.record.meal.enums.GlucoseGrade;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food", indexes = @Index(name = "idx_food_name", columnList = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private FoodSource source;

    /** CUSTOM 음식의 등록 회원. DB 음식은 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "brand", length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private FoodCategory category;

    @Column(name = "serving_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal servingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "serving_unit", nullable = false, length = 10)
    private FoodUnit servingUnit;

    @Embedded
    private Nutrients nutrients;

    @Enumerated(EnumType.STRING)
    @Column(name = "glucose_grade", length = 10)
    private GlucoseGrade glucoseGrade;

    public static Food dbFood(
        String name, String brand, FoodCategory category,
        BigDecimal servingAmount, FoodUnit servingUnit, Nutrients nutrients
    ) {
        Food food = new Food();
        food.source = FoodSource.DB;
        food.name = name;
        food.brand = brand;
        food.category = category;
        food.servingAmount = servingAmount;
        food.servingUnit = servingUnit;
        food.nutrients = nutrients;
        return food;
    }

    public static Food customFood(
        Member member, String name, String brand, BigDecimal servingAmount, FoodUnit servingUnit, Nutrients nutrients
    ) {
        Food food = new Food();
        food.source = FoodSource.CUSTOM;
        food.member = member;
        food.name = name;
        food.brand = brand;
        food.servingAmount = servingAmount;
        food.servingUnit = servingUnit;
        food.nutrients = nutrients;
        return food;
    }

    /**
     * DB 음식은 모두에게, 직접 등록 음식은 등록한 회원에게만 보인다.
     */
    public boolean isVisibleTo(Long memberId) {
        return source == FoodSource.DB || member.getId().equals(memberId);
    }
}
