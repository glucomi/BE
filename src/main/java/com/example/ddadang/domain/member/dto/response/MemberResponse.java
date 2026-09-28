package com.example.ddadang.domain.member.dto.response;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.DiabetesType;
import java.math.BigDecimal;

public record MemberResponse(
    Long memberId,
    String name,
    DiabetesType diabetesType,
    BigDecimal heightCm,
    Integer targetGlucoseMin,
    Integer targetGlucoseMax,
    boolean signupCompleted,
    boolean onboardingCompleted
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getDiabetesType(),
            member.getHeightCm(),
            member.getTargetGlucoseMin(),
            member.getTargetGlucoseMax(),
            member.isSignupCompleted(),
            member.isOnboardingCompleted()
        );
    }
}
