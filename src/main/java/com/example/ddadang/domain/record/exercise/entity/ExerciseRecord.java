package com.example.ddadang.domain.record.exercise.entity;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 운동 기록. 소모 열량은 기록 시점에 MET × 체중(kg) × 시간(h)으로 계산해 저장한다.
 */
@Entity
@Table(
    name = "exercise_record",
    indexes = @Index(name = "idx_exercise_record_member_performed_at", columnList = "member_id, performed_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    /** 체중 기록이 없으면 null */
    @Column(name = "kcal", precision = 8, scale = 2)
    private BigDecimal kcal;

    @Column(name = "memo", length = 1000)
    private String memo;

    public ExerciseRecord(
        Member member, Exercise exercise, LocalDateTime performedAt, Integer durationMin, BigDecimal weightKg,
        String memo
    ) {
        this.member = member;
        this.memo = memo;
        apply(exercise, performedAt, durationMin, weightKg);
    }

    public void update(
        Exercise exercise, LocalDateTime performedAt, Integer durationMin, BigDecimal weightKg, String memo
    ) {
        this.memo = memo;
        apply(exercise, performedAt, durationMin, weightKg);
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    private void apply(Exercise exercise, LocalDateTime performedAt, Integer durationMin, BigDecimal weightKg) {
        this.exercise = exercise;
        this.performedAt = performedAt;
        this.durationMin = durationMin;
        this.kcal = exercise.kcalFor(weightKg, durationMin);
    }
}
