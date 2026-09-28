package com.example.ddadang.domain.member.dto.response;

import com.example.ddadang.domain.member.entity.Member;

public record SignupStatusResponse(
    boolean signupCompleted,
    boolean onboardingCompleted
) {

    public static SignupStatusResponse from(Member member) {
        return new SignupStatusResponse(member.isSignupCompleted(), member.isOnboardingCompleted());
    }
}
