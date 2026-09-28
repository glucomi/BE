package com.example.ddadang.domain.member.controller;

import com.example.ddadang.domain.member.dto.request.OnboardingRequest;
import com.example.ddadang.domain.member.dto.response.MemberResponse;
import com.example.ddadang.domain.member.service.MemberService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "내 정보 조회 및 온보딩(MO-CURATION-010)")
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
}
