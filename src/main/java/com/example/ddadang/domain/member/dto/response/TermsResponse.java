package com.example.ddadang.domain.member.dto.response;

import com.example.ddadang.domain.member.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;

public record TermsResponse(
    TermsType termsType,
    String title,
    boolean required,
    @Schema(description = "약관 본문 화면(MO-SIGNUP-020)으로 이동 가능한 항목인지")
    boolean hasDocument,
    String version,
    @Schema(description = "약관 본문 URL. 문서 확정 전까지 null")
    String documentUrl
) {

    public static TermsResponse from(TermsType type) {
        return new TermsResponse(type, type.getTitle(), type.isRequired(), type.isHasDocument(), type.getVersion(), null);
    }
}
