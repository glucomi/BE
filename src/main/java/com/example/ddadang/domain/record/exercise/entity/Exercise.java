package com.example.ddadang.domain.record.exercise.entity;

import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 운동. 운동 DB(data/exercise.csv)에서 적재하며 원본코드(code)로 식별한다.
 */
@Entity
@Table(name = "exercise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise extends BaseTimeEntity {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 운동 DB 원본코드(Compendium of Physical Activities 코드). 운동 DB 밖에서 만든 운동은 null */
    @Column(name = "code", length = 10, unique = true)
    private String code;

    /** 분류 (예: 근력/웨이트, 걷기/러닝) */
    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 영문명(원문) */
    @Column(name = "name_en", length = 200)
    private String nameEn;

    /** 일문명(원문) */
    @Column(name = "name_ja", length = 200)
    private String nameJa;

    /** 소모 열량 계산용 MET */
    @Column(name = "met", nullable = false, precision = 4, scale = 1)
    private BigDecimal met;

    @Column(name = "popular", nullable = false)
    private boolean popular;

    public Exercise(String name, BigDecimal met, boolean popular) {
        this.name = name;
        this.met = met;
        this.popular = popular;
    }

    public Exercise(
        String code, String category, String name, String nameEn, String nameJa, BigDecimal met, boolean popular
    ) {
        this.code = code;
        update(category, name, nameEn, nameJa, met, popular);
    }

    public void update(String category, String name, String nameEn, String nameJa, BigDecimal met, boolean popular) {
        this.category = category;
        this.name = name;
        this.nameEn = nameEn;
        this.nameJa = nameJa;
        this.met = met;
        this.popular = popular;
    }

    /**
     * 소모 칼로리(kcal) = MET × 사용자 체중(kg) × (운동시간(분) ÷ 60), 소수 2자리 반올림. 체중을 모르면 null.
     */
    public BigDecimal kcalFor(BigDecimal weightKg, int durationMin) {
        if (weightKg == null) {
            return null;
        }
        return met.multiply(weightKg)
            .multiply(BigDecimal.valueOf(durationMin))
            .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP);
    }
}
