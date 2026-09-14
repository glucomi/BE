package com.example.ddadang.domain.cgm.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * GET /v1/public/events 응답 항목.
 * 공통 필드(event_type, event_at, value) 외 나머지는 event_type에 따라 선택적으로 채워진다.
 * - level: exercise (운동 세기 정도)
 * - measurement_type, medicine_type, name: medicine (약 관련)
 * - unit: exercise/insulin/ketone/meal/medicine
 * - meal_type: meal (영양소 타입)
 * - name: exercise (운동 강도 코드) / medicine (약 이름)
 * - insulin_type, name_code: insulin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CgmEventResponse(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("event_at") OffsetDateTime eventAt,
    Double value,
    Integer level,
    @JsonProperty("measurement_type") String measurementType,
    String unit,
    @JsonProperty("meal_type") String mealType,
    String name,
    @JsonProperty("insulin_type") String insulinType,
    @JsonProperty("name_code") String nameCode,
    @JsonProperty("medicine_type") String medicineType
) {
}
