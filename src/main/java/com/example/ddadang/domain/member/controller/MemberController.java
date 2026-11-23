package com.example.ddadang.domain.member.controller;

import com.example.ddadang.domain.member.dto.request.OnboardingRequest;
import com.example.ddadang.domain.member.dto.request.WithdrawalRequest;
import com.example.ddadang.domain.member.dto.response.MemberResponse;
import com.example.ddadang.domain.member.dto.response.NotificationSettingsResponse;
import com.example.ddadang.domain.member.service.MemberService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "내 정보 조회, 온보딩(MO-CURATION-010), 알림 설정(MO-ALARM-010), 회원 탈퇴(MO-PROFILE-020)")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "회원가입/온보딩 완료 여부와 온보딩 입력값을 조회한다.")
    @GetMapping("/api/members/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMe(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(memberService.getMe(memberId));
    }

    @Operation(
        summary = "온보딩 정보 입력",
        description = "큐레이션 4단계(당뇨 유형, 키, 몸무게, 목표 혈당 범위)를 한 번에 저장한다. "
            + "몸무게는 체중 기록으로 저장된다. 약관 동의 전이면 403, 목표 혈당 하한 >= 상한이면 400."
    )
    @PutMapping("/api/members/me/onboarding")
    public ResponseEntity<ApiResponse<MemberResponse>> completeOnboarding(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid OnboardingRequest request
    ) {
        return ApiResponse.success(memberService.completeOnboarding(memberId, request));
    }

    @Operation(summary = "알림 설정 조회", description = "혈당 관련 PUSH 알림 받기, 마케팅 수신 동의 여부를 조회한다.")
    @GetMapping("/api/members/me/notification-settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings(
        @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(memberService.getNotificationSettings(memberId));
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "탈퇴 사유를 저장하고 회원을 탈퇴 상태로 바꾼다. refresh token이 삭제되어 재발급이 불가하고, "
            + "탈퇴 후 30일 동안 같은 카카오 계정으로 로그인하면 403 AUTH_4031, 30일이 지나면 새 회원으로 가입된다. "
            + "이미 탈퇴한 회원이면 403 AUTH_4030."
    )
    @PostMapping("/api/members/me/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid WithdrawalRequest request
    ) {
        memberService.withdraw(memberId, request);
        return ApiResponse.success(null);
    }
}
