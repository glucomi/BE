package com.example.ddadang.domain.member.dto.response;

import com.example.ddadang.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    Long memberId,
    String accessToken,
    String refreshToken,
    @Schema(description = "이번 로그인으로 새로 가입된 회원인지")
    boolean isNewMember,
    @Schema(description = "약관 동의(MO-SIGNUP) 완료 여부. false면 회원가입 화면으로 이동")
    boolean signupCompleted,
    @Schema(description = "온보딩(MO-CURATION) 완료 여부. false면 큐레이션 화면으로 이동")
    boolean onboardingCompleted
) {

    public static LoginResponse of(Member member, TokenResponse tokens, boolean isNewMember) {
        return new LoginResponse(
            member.getId(),
            tokens.accessToken(),
            tokens.refreshToken(),
            isNewMember,
            member.isSignupCompleted(),
            member.isOnboardingCompleted()
        );
    }
}
