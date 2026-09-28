package com.example.ddadang.domain.cgm.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * 센서 정보 조회 응답. 정확한 엔드포인트 경로가 미확인 상태(doc115, 추정 /v1/public/sensors)라
 * 필드 형태만 관찰된 예시를 기준으로 정의해두었다. REST API 테스트 탭에서 확정 필요.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorInfoResponse(
    @JsonProperty("serial_number") String serialNumber,
    @JsonProperty("started_at") OffsetDateTime startedAt,
    @JsonProperty("ended_at") OffsetDateTime endedAt
) {
}
