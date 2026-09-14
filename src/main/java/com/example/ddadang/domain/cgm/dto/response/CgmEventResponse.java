package com.example.ddadang.domain.cgm.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * GET /v1/public/events 응답 항목 하나.
 *
 * <p>주의: 개발가이드 문서상 예시는 평평한 배열({@code [{...}, {...}]})이지만, 실제 샌드박스 응답은
 * 카테고리(bgm/exercise/ketone/meal 등)를 키로 하는 객체({@code {"bgm": [...], "exercise": [...]}})로
 * 내려온다. 카테고리 하나 안에도 더 세부적인 event_type(예: "ketone" 카테고리 안에 "gki", "bmi")이
 * 섞여 있을 수 있어, 실제 이벤트 종류는 카테고리 키가 아니라 각 항목의 {@link #eventType()}로 판단해야 한다.
 * 문서에 없던 tz_offset 필드도 실제로 내려온다.
 *
 * <p>공통 필드(event_type, event_at, tz_offset, value) 외 나머지는 event_type에 따라 선택적으로 채워진다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CgmEventResponse(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("event_at") OffsetDateTime eventAt,
    @JsonProperty("tz_offset") Integer tzOffset,
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
