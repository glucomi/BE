package com.example.ddadang.domain.cgm.controller;

import com.example.ddadang.domain.cgm.entity.CgmToken;
import com.example.ddadang.domain.cgm.service.IsensAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CgmAuthController {

    private static final String STATE_SESSION_KEY = "isens_oauth_state";

    private final IsensAuthService isensAuthService;

    /**
     * 아이센스 로그인 페이지로 리다이렉트. state를 세션에 저장해두고 콜백에서 CSRF 검증에 사용한다.
     */
    @GetMapping("/api/cgm/oauth/authorize")
    public void authorize(HttpSession session, HttpServletResponse response) throws IOException {
        String state = isensAuthService.generateState();
        session.setAttribute(STATE_SESSION_KEY, state);
        response.sendRedirect(isensAuthService.buildAuthorizeUrl(state));
    }

    @GetMapping("/api/cgm/oauth/callback")
    public CgmOAuthCallbackResult callback(
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
        return new CgmOAuthCallbackResult(token.getIsensUserId());
    }

    public record CgmOAuthCallbackResult(String isensUserId) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidOAuthStateException extends RuntimeException {
        public InvalidOAuthStateException() {
            super("state 값이 일치하지 않습니다. CSRF 공격이 의심되거나 세션이 만료되었습니다.");
        }
    }
}
