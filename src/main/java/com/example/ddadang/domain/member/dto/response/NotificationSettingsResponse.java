package com.example.ddadang.domain.member.dto.response;

import com.example.ddadang.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationSettingsResponse(
    @Schema(description = "혈당 관련 PUSH 알림 받기") boolean pushGlucoseEnabled,
    @Schema(description = "마케팅 수신 동의(혜택, 이벤트, 프로모션 정보)") boolean marketingAgreed
) {

    public static NotificationSettingsResponse from(Member member) {
        return new NotificationSettingsResponse(member.isPushGlucoseEnabled(), member.isMarketingAgreed());
    }
}
