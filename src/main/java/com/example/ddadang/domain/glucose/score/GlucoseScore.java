package com.example.ddadang.domain.glucose.score;

/**
 * 계산값은 반올림하지 않은 소수로 보관한다. 화면 표시용 반올림은 응답 변환 시점에만 한다.
 *
 * @param score      0~100 소수. FINAL/PROVISIONAL일 때만 값이 있다
 * @param deductions 항목별 감점(양수, 소수). 점수가 없으면 null
 */
public record GlucoseScore(
    GlucoseScoreStatus status,
    Double score,
    Deductions deductions
) {

    public static GlucoseScore withoutScore(GlucoseScoreStatus status) {
        return new GlucoseScore(status, null, null);
    }

    public record Deductions(double tir, double meanGlucose, double cv, double spike, double hypoglycemia) {

        double total() {
            return tir + meanGlucose + cv + spike + hypoglycemia;
        }
    }
}
