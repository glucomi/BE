package com.example.ddadang.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MO-SIGNUP-010 약관 목록. 약관 문구가 바뀌면 version을 올려 동의 이력(member_agreement)에 남긴다.
 * hasDocument=true인 항목은 MO-SIGNUP-020 본문 화면으로 이동할 수 있다.
 */
@Getter
@RequiredArgsConstructor
public enum TermsType {
    AGE_OVER_14("만 14세 이상입니다", true, false, "1.0"),
    SERVICE("이용약관", true, true, "1.0"),
    PRIVACY("개인정보 수집 및 이용", true, true, "1.0"),
    SERVICE_IMPROVEMENT("서비스 품질 향상", false, true, "1.0"),
    MARKETING("이벤트 및 혜택 알림 수신", false, false, "1.0");

    private final String title;
    private final boolean required;
    private final boolean hasDocument;
    private final String version;
}
