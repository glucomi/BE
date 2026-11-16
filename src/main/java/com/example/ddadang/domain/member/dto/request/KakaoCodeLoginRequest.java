package com.example.ddadang.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KakaoCodeLoginRequest(
    @Schema(description = "카카오 로그인 후 redirect URI로 전달받은 인가 코드(code 쿼리 파라미터)")
    @NotBlank String code,
    @Schema(
        description = "인가 코드를 받을 때 사용한 redirect URI. 카카오 인가 요청(/oauth/authorize)에 넣은 값과 정확히 같아야 한다.",
        example = "https://ddadang.vercel.app/auth/kakao/callback"
    )
    @NotBlank String redirectUri
) {
}
