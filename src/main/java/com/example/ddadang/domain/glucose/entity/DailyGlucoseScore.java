package com.example.ddadang.domain.glucose.entity;

import com.example.ddadang.domain.glucose.score.DailyGlucoseAnalysis;
import com.example.ddadang.domain.glucose.score.GlucoseGroup;
import com.example.ddadang.domain.glucose.score.GlucoseScore;
import com.example.ddadang.domain.glucose.score.GlucoseScoreStatus;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 확정(freeze)된 날짜별 혈당 점수. 저장 이후에는 늦게 들어온 CGM 데이터나 파라미터 변경이 있어도 값이 바뀌지 않는다.
 * 계산값은 반올림 전 소수로 저장한다.
 */
@Entity
@Table(
    name = "daily_glucose_score",
    uniqueConstraints = @UniqueConstraint(name = "uk_daily_glucose_score_member_date", columnNames = {"member_id", "score_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyGlucoseScore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "glucose_group", nullable = false, length = 20)
    private GlucoseGroup glucoseGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GlucoseScoreStatus status;

    @Column(name = "score")
    private Double score;

    @Column(name = "average_glucose")
    private Double averageGlucose;

    @Column(name = "spike_count")
    private Integer spikeCount;

    @Column(name = "tir_deduction")
    private Double tirDeduction;

    @Column(name = "mean_deduction")
    private Double meanDeduction;

    @Column(name = "cv_deduction")
    private Double cvDeduction;

    @Column(name = "spike_deduction")
    private Double spikeDeduction;

    @Column(name = "hypo_deduction")
    private Double hypoDeduction;

    @Column(name = "finalized_at", nullable = false)
    private LocalDateTime finalizedAt;

    public static DailyGlucoseScore freeze(
        Member member, LocalDate date, GlucoseGroup group, DailyGlucoseAnalysis analysis, LocalDateTime finalizedAt
    ) {
        DailyGlucoseScore daily = new DailyGlucoseScore();
        daily.member = member;
        daily.scoreDate = date;
        daily.glucoseGroup = group;
        daily.status = analysis.score().status();
        daily.score = analysis.score().score();
        daily.averageGlucose = analysis.averageGlucose();
        daily.spikeCount = analysis.spikeCount();
        GlucoseScore.Deductions d = analysis.score().deductions();
        if (d != null) {
            daily.tirDeduction = d.tir();
            daily.meanDeduction = d.meanGlucose();
            daily.cvDeduction = d.cv();
            daily.spikeDeduction = d.spike();
            daily.hypoDeduction = d.hypoglycemia();
        }
        daily.finalizedAt = finalizedAt;
        return daily;
    }

    public DailyGlucoseAnalysis toAnalysis() {
        GlucoseScore.Deductions deductions = tirDeduction == null ? null : new GlucoseScore.Deductions(
            tirDeduction, meanDeduction, cvDeduction, spikeDeduction, hypoDeduction
        );
        return new DailyGlucoseAnalysis(averageGlucose, spikeCount, new GlucoseScore(status, score, deductions));
    }
}
