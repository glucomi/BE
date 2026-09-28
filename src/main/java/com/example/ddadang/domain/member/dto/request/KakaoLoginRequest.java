package com.example.ddadang.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
    @Schema(description = "카카오 SDK 로그인으로 받은 카카오 access token")
    @NotBlank String kakaoAccessToken
) {
}
