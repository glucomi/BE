package com.example.ddadang.domain.record.weight.entity;

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

@Entity
@Table(
    name = "weight_record",
    indexes = @Index(name = "idx_weight_record_member_measured_at", columnList = "member_id, measured_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeightRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 1)
    private BigDecimal weightKg;

    public WeightRecord(Member member, LocalDateTime measuredAt, BigDecimal weightKg) {
        this.member = member;
        this.measuredAt = measuredAt;
        this.weightKg = weightKg;
    }

    public void update(LocalDateTime measuredAt, BigDecimal weightKg) {
        this.measuredAt = measuredAt;
        this.weightKg = weightKg;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
