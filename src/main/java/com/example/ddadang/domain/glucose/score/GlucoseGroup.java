package com.example.ddadang.domain.glucose.score;

import com.example.ddadang.domain.member.enums.DiabetesType;

/**
 * 혈당 점수 산출용 그룹. 그룹별 파라미터는 {@link GlucoseAnalysisProperties#groups()}에서 관리한다.
 * 점수는 회원이 설정한 목표 범위가 아니라 그룹 목표 범위 기준으로 계산한다.
 */
public enum GlucoseGroup {
    NON_DM, PRE_DM, DM, GDM;

    public static GlucoseGroup from(DiabetesType diabetesType) {
        if (diabetesType == null) {
            return NON_DM;
        }
        return switch (diabetesType) {
            case NONE -> NON_DM;
            case PRE -> PRE_DM;
            case TYPE1, TYPE2_INSULIN, TYPE2_NO_INSULIN -> DM;
            case GESTATIONAL -> GDM;
        };
    }
}
