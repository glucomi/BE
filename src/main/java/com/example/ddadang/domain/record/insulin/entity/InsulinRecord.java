package com.example.ddadang.domain.record.insulin.entity;

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
    name = "insulin_record",
    indexes = @Index(name = "idx_insulin_record_member_injected_at", columnList = "member_id, injected_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsulinRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_insulin_id", nullable = false)
    private MemberInsulin memberInsulin;

    @Column(name = "injected_at", nullable = false)
    private LocalDateTime injectedAt;

    @Column(name = "dose_unit", nullable = false, precision = 5, scale = 1)
    private BigDecimal doseUnit;

    @Column(name = "memo", length = 1000)
    private String memo;

    public InsulinRecord(
        Member member, MemberInsulin memberInsulin, LocalDateTime injectedAt, BigDecimal doseUnit, String memo
    ) {
        this.member = member;
        this.memberInsulin = memberInsulin;
        this.injectedAt = injectedAt;
        this.doseUnit = doseUnit;
        this.memo = memo;
    }

    public void update(MemberInsulin memberInsulin, LocalDateTime injectedAt, BigDecimal doseUnit, String memo) {
        this.memberInsulin = memberInsulin;
        this.injectedAt = injectedAt;
        this.doseUnit = doseUnit;
        this.memo = memo;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
