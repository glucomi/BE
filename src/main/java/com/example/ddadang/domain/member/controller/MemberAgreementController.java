package com.example.ddadang.domain.member.controller;

import com.example.ddadang.domain.member.dto.request.AgreementRequest;
import com.example.ddadang.domain.member.dto.response.SignupStatusResponse;
import com.example.ddadang.domain.member.dto.response.TermsResponse;
import com.example.ddadang.domain.member.service.MemberAgreementService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원가입", description = "약관 목록 조회 및 약관 동의 (MO-SIGNUP-010, 020)")
@RestController
@RequiredArgsConstructor
public class MemberAgreementController {

    private final MemberAgreementService memberAgreementService;

    @Operation(summary = "약관 목록 조회", description = "회원가입 화면에 노출할 약관 목록(필수/선택, 본문 이동 가능 여부)을 조회한다.")
    @GetMapping("/api/terms")
    public ResponseEntity<ApiResponse<List<TermsResponse>>> getTerms() {
        return ApiResponse.success(memberAgreementService.getTerms());
    }

    @Operation(
        summary = "약관 동의",
        description = "약관별 동의 여부를 저장하고 회원가입을 완료한다. 필수 약관(만 14세 이상/이용약관/개인정보)에 "
            + "모두 동의하지 않으면 400. 이벤트 및 혜택 알림 수신(MARKETING) 동의는 마케팅 수신 설정에도 반영된다."
    )
    @PostMapping("/api/members/me/agreements")
    public ResponseEntity<ApiResponse<SignupStatusResponse>> agree(
        @AuthenticationPrincipal Long memberId,
        @RequestBody @Valid AgreementRequest request
    ) {
        return ApiResponse.success(memberAgreementService.agree(memberId, request));
    }
}
