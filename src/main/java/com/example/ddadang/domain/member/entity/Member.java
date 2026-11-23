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
import java.time.Duration;
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

    public void completeOnboarding(
        DiabetesType diabetesType, BigDecimal heightCm, Integer targetGlucoseMin, Integer targetGlucoseMax
    ) {
        this.diabetesType = diabetesType;
        this.heightCm = heightCm;
        this.targetGlucoseMin = targetGlucoseMin;
        this.targetGlucoseMax = targetGlucoseMax;
        this.onboardingCompleted = true;
    }

    public void completeSignup(boolean marketingAgreed) {
        this.signupCompleted = true;
        this.marketingAgreed = marketingAgreed;
    }

    public void withdraw(LocalDateTime now) {
        this.status = MemberStatus.WITHDRAWN;
        this.deletedAt = now;
    }

    public boolean isWithdrawn() {
        return status == MemberStatus.WITHDRAWN;
    }

    /** 탈퇴 후 restriction 기간이 지났는지 */
    public boolean canRejoin(LocalDateTime now, Duration restriction) {
        return isWithdrawn() && deletedAt != null && !deletedAt.plus(restriction).isAfter(now);
    }

    /**
     * 재가입 시 같은 소셜 계정으로 새 회원을 만들 수 있도록 탈퇴 회원의 provider_id를 비운다(유니크 제약 해제).
     * 원래 값은 "withdrawn:{회원ID}:{provider_id}" 형태로 남긴다.
     */
    public void releaseProviderId() {
        String released = "withdrawn:" + id + ":" + providerId;
        this.providerId = released.length() > 100 ? released.substring(0, 100) : released;
    }
}
