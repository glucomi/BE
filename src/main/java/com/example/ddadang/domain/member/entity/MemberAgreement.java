package com.example.ddadang.domain.member.entity;

import com.example.ddadang.domain.member.enums.TermsType;
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
 * 약관 동의 이력. 동의를 변경할 때마다 새 행을 쌓는다.
 */
@Entity
@Table(name = "member_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 30)
    private TermsType termsType;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @Column(name = "agreed", nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    public MemberAgreement(Member member, TermsType termsType, boolean agreed, LocalDateTime agreedAt) {
        this.member = member;
        this.termsType = termsType;
        this.termsVersion = termsType.getVersion();
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }
}
