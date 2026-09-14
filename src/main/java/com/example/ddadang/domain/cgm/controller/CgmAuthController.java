package com.example.ddadang.domain.cgm.controller;

import com.example.ddadang.domain.cgm.entity.CgmToken;
import com.example.ddadang.domain.cgm.exception.InvalidOAuthStateException;
import com.example.ddadang.domain.cgm.service.IsensAuthService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CGM 인증", description = "아이센스 OAuth 2.0 인증 플로우 (로그인 리다이렉트 / 콜백)")
@RestController
@RequiredArgsConstructor
public class CgmAuthController {

    private static final String STATE_SESSION_KEY = "isens_oauth_state";

    private final IsensAuthService isensAuthService;

    @Operation(
        summary = "아이센스 로그인으로 리다이렉트",
        description = "브라우저 주소창에서 직접 열어야 함(Swagger 'Try it out'은 리다이렉트를 못 따라감). "
            + "state를 세션에 저장해두고 콜백에서 CSRF 검증에 사용한다."
    )
    @GetMapping("/api/cgm/oauth/authorize")
    public void authorize(HttpSession session, HttpServletResponse response) throws IOException {
        String state = isensAuthService.generateState();
        session.setAttribute(STATE_SESSION_KEY, state);
        response.sendRedirect(isensAuthService.buildAuthorizeUrl(state));
    }

    @Operation(
        summary = "인가 코드 콜백 처리 (토큰 교환)",
        description = "아이센스 로그인 성공 후 리다이렉트되는 콜백. state를 검증하고 인가 코드를 "
            + "access token / refresh token으로 교환해 저장한 뒤 isensUserId를 반환한다."
    )
    @GetMapping("/api/cgm/oauth/callback")
    public ResponseEntity<ApiResponse<CgmOAuthCallbackResult>> callback(
        @RequestParam String code,
        @RequestParam String state,
        HttpSession session
    ) {
        Object savedState = session.getAttribute(STATE_SESSION_KEY);
        if (savedState == null || !savedState.equals(state)) {
            throw new InvalidOAuthStateException();
        }
        session.removeAttribute(STATE_SESSION_KEY);

        CgmToken token = isensAuthService.exchangeCodeForToken(code);
        return ApiResponse.success(new CgmOAuthCallbackResult(token.getIsensUserId()));
    }

    public record CgmOAuthCallbackResult(String isensUserId) {
    }
}
