package com.example.ddadang.domain.glucose.score;

/**
 * @param score     0~100. FINAL/PROVISIONAL일 때만 값이 있다
 * @param deductions 항목별 감점(양수). 점수가 없으면 null
 */
public record GlucoseScore(
    GlucoseScoreStatus status,
    Integer score,
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
