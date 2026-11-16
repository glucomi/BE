package com.example.ddadang.domain.member.controller;

import com.example.ddadang.domain.member.dto.request.KakaoCodeLoginRequest;
import com.example.ddadang.domain.member.dto.request.KakaoLoginRequest;
import com.example.ddadang.domain.member.dto.request.TokenReissueRequest;
import com.example.ddadang.domain.member.dto.response.LoginResponse;
import com.example.ddadang.domain.member.dto.response.TokenResponse;
import com.example.ddadang.domain.member.service.AuthService;
import com.example.ddadang.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "카카오 로그인 및 토큰 재발급")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "카카오 로그인 (앱)",
        description = "앱에서 카카오 SDK로 받은 카카오 access token을 전달하면 회원 조회/가입 후 JWT를 발급한다. "
            + "signupCompleted=false면 약관 동의(MO-SIGNUP), onboardingCompleted=false면 큐레이션(MO-CURATION)으로 이동한다."
    )
    @PostMapping("/api/auth/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithKakao(@RequestBody @Valid KakaoLoginRequest request) {
        return ApiResponse.success(authService.loginWithKakao(request.kakaoAccessToken()));
    }

    @Operation(
        summary = "카카오 로그인 (웹, 인가 코드)",
        description = "웹에서 카카오 로그인 후 redirect URI로 받은 인가 코드(code)를 전달하면 서버가 카카오 토큰으로 교환한 뒤 "
            + "회원 조회/가입 후 JWT를 발급한다. 응답은 카카오 로그인(앱)과 같다. "
            + "redirectUri는 카카오 인가 요청에 쓴 값과 정확히 같아야 하고, 인가 코드는 한 번만 쓸 수 있다. "
            + "코드가 만료·재사용됐거나 redirectUri가 다르면 401 AUTH_4014."
    )
    @PostMapping("/api/auth/kakao/code")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithKakaoCode(
        @RequestBody @Valid KakaoCodeLoginRequest request
    ) {
        return ApiResponse.success(authService.loginWithKakaoCode(request.code(), request.redirectUri()));
    }

    @Operation(
        summary = "토큰 재발급",
        description = "refresh token으로 access/refresh token을 새로 발급한다. 사용한 refresh token은 폐기된다(rotation)."
    )
    @PostMapping("/api/auth/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestBody @Valid TokenReissueRequest request) {
        return ApiResponse.success(authService.reissue(request.refreshToken()));
    }
}
