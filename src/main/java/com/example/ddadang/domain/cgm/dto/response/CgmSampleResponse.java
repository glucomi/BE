package com.example.ddadang.domain.cgm.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * GET /v1/public/cgms 응답 항목.
 *
 * <p>stage: 1=스무딩 진행 중(동일 serial_number+seq_number 값이 최대 6회까지 미세 조정될 수 있어
 * stage=2 확정 전까지 덮어쓰기 필요, {@link com.example.ddadang.domain.cgm.entity.CgmReading#updateFrom}
 * 참고), 2=스무딩 완료(확정).<br>
 * trend: 0=Unknown, 1=빠르게 감소, 2=감소, 3=서서히 감소, 4=안정적, 5=서서히 증가, 6=증가, 7=빠르게 증가.<br>
 * min_max_flag: 0=정상범위(40~500), 1=40 미만, 2=500 초과.<br>
 * error_code: 상세 enum 매핑표 미확인, raw int로 보관.
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
