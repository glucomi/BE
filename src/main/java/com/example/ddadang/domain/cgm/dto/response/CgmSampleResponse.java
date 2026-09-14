package com.example.ddadang.domain.cgm.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * GET /v1/public/cgms 응답 항목.
 * stage / trend / error_code / min_max_flag의 enum 의미는 원문 표(doc116) 미확인 상태라
 * 우선 raw int로 받는다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CgmSampleResponse(
    @JsonProperty("serial_number") String serialNumber,
    @JsonProperty("seq_number") Long seqNumber,
    @JsonProperty("event_at") OffsetDateTime eventAt,
    @JsonProperty("tz_offset") Integer tzOffset,
    Integer stage,
    @JsonProperty("initial_value") Double initialValue,
    Double value,
    @JsonProperty("trend_rate") Double trendRate,
    Integer trend,
    @JsonProperty("error_code") Integer errorCode,
    @JsonProperty("min_max_flag") Integer minMaxFlag
) {
}
