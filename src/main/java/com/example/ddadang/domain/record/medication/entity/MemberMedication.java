package com.example.ddadang.domain.record.medication.entity;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.record.medication.enums.MedicationCategory;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원이 등록한 "내 복용약". 복약 기록은 이 중 하나를 골라 남긴다.
 * 삭제해도 과거 복약 기록이 약 정보를 보여줄 수 있도록 soft delete 한다.
 */
@Entity
@Table(name = "member_medication")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMedication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private MedicationCategory category;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MemberMedication(Member member, MedicationCategory category, String productName) {
        this.member = member;
        this.category = category;
        this.productName = productName;
    }

    public void update(MedicationCategory category, String productName) {
        this.category = category;
        this.productName = productName;
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
