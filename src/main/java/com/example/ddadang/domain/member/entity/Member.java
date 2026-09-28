package com.example.ddadang.domain.member.entity;

import com.example.ddadang.domain.member.enums.DiabetesType;
import com.example.ddadang.domain.member.enums.Gender;
import com.example.ddadang.domain.member.enums.MemberStatus;
import com.example.ddadang.domain.member.enums.SocialProvider;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "member",
    uniqueConstraints = @UniqueConstraint(name = "uk_member_provider", columnNames = {"provider", "provider_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "diabetes_type", length = 30)
    private DiabetesType diabetesType;

    @Column(name = "target_glucose_min")
    private Integer targetGlucoseMin;

    @Column(name = "target_glucose_max")
    private Integer targetGlucoseMax;

    @Column(name = "signup_completed", nullable = false)
    private boolean signupCompleted;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "push_glucose_enabled", nullable = false)
    private boolean pushGlucoseEnabled;

    @Column(name = "marketing_agreed", nullable = false)
    private boolean marketingAgreed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Member createSocialMember(SocialProvider provider, String providerId, String name) {
        Member member = new Member();
        member.provider = provider;
        member.providerId = providerId;
        member.name = name;
        member.pushGlucoseEnabled = true;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public void completeSignup(boolean marketingAgreed) {
        this.signupCompleted = true;
        this.marketingAgreed = marketingAgreed;
    }
}
