package com.example.ddadang.domain.member.dto.request;

import com.example.ddadang.domain.member.enums.WithdrawalReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record WithdrawalRequest(
    @Schema(
        description = "탈퇴 사유. RARELY_USED: 자주 사용하지 않게 됐어요, MISSING_FEATURE: 원하는 기능이 없어요, "
            + "DEVICE_INCONVENIENT: 혈당 기기 연동/측정이 불편해요, HARD_TO_USE: 사용법이 어렵고 복잡해요, "
            + "PRIVACY_CONCERN: 개인정보/데이터 보안이 걱정돼요",
        example = "RARELY_USED"
    )
    @NotNull WithdrawalReason reason
) {
}
