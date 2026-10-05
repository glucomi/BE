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

@Entity
@Table(name = "exercise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise extends BaseTimeEntity {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

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

    /**
     * 소모 열량(kcal) = MET × 체중(kg) × 시간(h), 소수 2자리 반올림. 체중을 모르면 null.
     * TODO: 정책서의 "칼로리 산출 노션 참고" 문서가 정해지면 산출식 확인.
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
