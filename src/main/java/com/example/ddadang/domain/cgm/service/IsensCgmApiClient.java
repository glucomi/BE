package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.cgm.dto.response.SensorInfoResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * api.i-sens.com 호출만 담당하는 얇은 클라이언트. 토큰 갱신/저장 등 비즈니스 로직은
 * {@link CgmSyncService}, {@link IsensAuthService}에서 처리한다.
 */
@Component
@RequiredArgsConstructor
public class IsensCgmApiClient {

    @Qualifier("isensApiRestClient")
    private final RestClient isensApiRestClient;

    /**
     * GET /v1/public/cgms — 최대 3개월 범위 제한이 있으므로 호출 전 CgmDateRangeSplitter로 쪼개서 넣어야 한다.
     */
    public List<CgmSampleResponse> fetchCgmData(String accessToken, OffsetDateTime start, OffsetDateTime end) {
        return isensApiRestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/public/cgms")
                .queryParam("start", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(start))
                .queryParam("end", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(end))
                .build())
            .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
            .retrieve()
            .body(new ParameterizedTypeReference<List<CgmSampleResponse>>() {
            });
    }

    /**
     * POST /v1/public/samples — 샌드박스 전용 테스트 데이터 생성. 파라미터 없이 헤더만으로 호출된다.
     */
    public void generateSandboxSamples(String accessToken) {
        isensApiRestClient.post()
            .uri("/v1/public/samples")
            .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
            .retrieve()
            .toBodilessEntity();
    }

    /**
     * TODO: 정확한 엔드포인트 경로 미확인 (doc115, 추정 /v1/public/sensors).
     * REST API 테스트 탭에서 실제 경로/응답을 확인한 뒤 검증할 것.
     */
    public List<SensorInfoResponse> fetchSensors(String accessToken) {
        return isensApiRestClient.get()
            .uri("/v1/public/sensors")
            .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
            .retrieve()
            .body(new ParameterizedTypeReference<List<SensorInfoResponse>>() {
            });
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
