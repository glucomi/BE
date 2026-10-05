package com.example.ddadang.domain.record.insulin.entity;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원이 등록한 "내 인슐린". 인슐린 기록은 이 중 하나를 골라 남긴다.
 * 삭제해도 과거 인슐린 기록이 제품명을 보여줄 수 있도록 soft delete 한다.
 */
@Entity
@Table(name = "member_insulin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInsulin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insulin_product_id", nullable = false)
    private InsulinProduct insulinProduct;

    @Column(name = "default_dose_unit", nullable = false, precision = 5, scale = 1)
    private BigDecimal defaultDoseUnit;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MemberInsulin(Member member, InsulinProduct insulinProduct, BigDecimal defaultDoseUnit) {
        this.member = member;
        this.insulinProduct = insulinProduct;
        this.defaultDoseUnit = defaultDoseUnit;
    }

    public void changeDefaultDose(BigDecimal defaultDoseUnit) {
        this.defaultDoseUnit = defaultDoseUnit;
    }

    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
