package com.example.ddadang.domain.record.exercise.entity;

import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise extends BaseTimeEntity {

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
}
