package com.example.ddadang.domain.glucose.score;

import com.example.ddadang.domain.member.enums.DiabetesType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 혈당 점수 산출용 그룹. 목표 범위/허용치/감점 계수가 그룹별로 다르다.
 * 점수는 회원이 설정한 목표 범위가 아니라 그룹 목표 범위 기준으로 계산한다.
 */
@Getter
@RequiredArgsConstructor
public enum GlucoseGroup {
    //       목표범위   TIR목표 TBR1허용 TBR2허용 경도저혈당상한  평균(start, slope)  CV(start, slope)
    NON_DM(70, 140, 95.0, 0.0, 0.0, 70, 100, 1.0, 12, 0.45),
    PRE_DM(70, 140, 85.0, 4.0, 1.0, 70, 100, 0.53, 12, 0.45),
    DM(70, 180, 70.0, 4.0, 1.0, 70, 120, 0.29, 14, 0.60),
    GDM(63, 140, 70.0, 4.0, 1.0, 63, 92, 0.55, 12, 0.45);

    /** 초저혈당(TBR2) 기준: 54 미만 */
    public static final int SEVERE_HYPO_BELOW = 54;

    private final int rangeLow;
    private final int rangeHigh;
    private final double tirTargetPercent;
    private final double tbr1AllowedPercent;
    private final double tbr2AllowedPercent;
    /** 경도 저혈당(TBR1) 상한(미만). 54 ≤ 값 < mildHypoBelow */
    private final int mildHypoBelow;
    private final double meanStart;
    private final double meanSlope;
    private final double cvStart;
    private final double cvSlope;

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
