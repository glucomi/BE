package com.example.ddadang.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record KakaoAuthorizeUrlResponse(
    @Schema(description = "카카오 로그인 페이지 주소. 이 주소를 그대로 연다.") String authorizeUrl,
    @Schema(description = "인가 URL에 넣은 state. 요청에 state를 넘겼으면 같은 값, 아니면 서버가 만든 값. 콜백의 state와 비교한다.") String state
) {
}
