package com.example.ddadang.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
    @NotBlank String refreshToken
) {
}
