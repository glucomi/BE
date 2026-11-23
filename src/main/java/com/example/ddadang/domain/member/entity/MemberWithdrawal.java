package com.example.ddadang.domain.member.entity;

import com.example.ddadang.domain.member.enums.WithdrawalReason;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 이력. 탈퇴 사유 집계용으로 남긴다.
 */
@Entity
@Table(name = "member_withdrawal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberWithdrawal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private WithdrawalReason reason;

    public MemberWithdrawal(Long memberId, WithdrawalReason reason) {
        this.memberId = memberId;
        this.reason = reason;
    }
}
