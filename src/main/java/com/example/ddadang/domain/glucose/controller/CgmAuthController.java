package com.example.ddadang.domain.glucose.controller;

import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.service.IsensAuthService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CGM 인증", description = "아이센스 OAuth 2.0 연결 (인가 URL 발급 / 콜백)")
@RestController
@RequiredArgsConstructor
public class CgmAuthController {

    private final IsensAuthService isensAuthService;

    @Operation(
        summary = "아이센스 인가 URL 발급",
        description = "로그인 회원 기준으로 아이센스 로그인 URL을 발급한다. 앱은 이 URL을 브라우저(웹뷰)로 열고, "
            + "로그인이 끝나면 콜백으로 리다이렉트된다. state에 회원 정보가 서명되어 있어 10분 안에 완료해야 한다."
    )
    @GetMapping("/api/cgm/oauth/authorize-url")
    public ResponseEntity<ApiResponse<AuthorizeUrlResponse>> getAuthorizeUrl(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(new AuthorizeUrlResponse(isensAuthService.buildAuthorizeUrl(memberId)));
    }

    @Operation(
        summary = "인가 코드 콜백 처리 (CGM 연결)",
        description = "아이센스 로그인 성공 후 리다이렉트되는 콜백(인증 헤더 없이 호출됨). state로 회원을 확인하고 "
            + "인가 코드를 토큰으로 교환해 회원의 CGM 연결을 저장한다. 이미 다른 회원에게 연결된 계정이면 409."
    )
    @GetMapping("/api/cgm/oauth/callback")
    public ResponseEntity<ApiResponse<CgmConnectResult>> callback(
        @RequestParam String code,
        @RequestParam String state
    ) {
        Long memberId = isensAuthService.resolveMemberIdFromState(state);
        CgmConnection connection = isensAuthService.connect(memberId, code);
        return ApiResponse.success(new CgmConnectResult(connection.getProvider(), true));
    }

    public record AuthorizeUrlResponse(String authorizeUrl) {
    }

    public record CgmConnectResult(CgmProvider provider, boolean connected) {
    }
}
