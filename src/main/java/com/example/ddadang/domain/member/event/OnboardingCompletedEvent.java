package com.example.ddadang.domain.member.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 온보딩(MO-CURATION-010) 완료 시 발행. 입력받은 몸무게는 record 도메인이 첫 체중 기록으로 저장한다.
 * member는 record를 모르도록(의존 방향 record → member) 이벤트로 넘긴다.
 */
public record OnboardingCompletedEvent(
    Long memberId,
    BigDecimal weightKg,
    LocalDateTime completedAt
) {
}
