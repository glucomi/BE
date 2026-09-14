package com.example.ddadang.domain.cgm.controller;

import com.example.ddadang.domain.cgm.dto.response.CgmReadingResponse;
import com.example.ddadang.domain.cgm.dto.response.CgmSyncResultResponse;
import com.example.ddadang.domain.cgm.dto.response.SandboxSampleResponse;
import com.example.ddadang.domain.cgm.repository.CgmReadingRepository;
import com.example.ddadang.domain.cgm.service.CgmSyncService;
import com.example.ddadang.domain.cgm.service.IsensAuthService;
import com.example.ddadang.domain.cgm.service.IsensCgmApiClient;
import com.example.ddadang.global.response.ApiResponse;
import com.example.ddadang.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * isensUserId를 요청 파라미터로 직접 받는 것은 임시 조치이다.
 * TODO: Member 도메인/인증이 붙으면 로그인한 사용자 principal에서 isensUserId를 조회하도록 교체.
 */
@Tag(name = "CGM 데이터", description = "i-sens CGM 데이터 동기화 및 자체 DB 기준 조회")
@RestController
@RequiredArgsConstructor
public class CgmController {

    private final CgmSyncService cgmSyncService;
    private final CgmReadingRepository cgmReadingRepository;
    private final IsensAuthService isensAuthService;
    private final IsensCgmApiClient isensCgmApiClient;

    @Operation(
        summary = "CGM 데이터 동기화",
        description = "i-sens /v1/public/cgms에서 start~end 구간 데이터를 가져와 자체 DB에 저장한다. "
            + "3개월 초과 구간은 자동으로 분할 호출 후 병합하고, 이미 저장된 시리얼+순번은 건너뛴다."
    )
    @PostMapping("/api/cgm/sync")
    public ResponseEntity<ApiResponse<CgmSyncResultResponse>> sync(
        @RequestParam String isensUserId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        return ApiResponse.success(cgmSyncService.syncCgmData(isensUserId, start, end));
    }

    @Operation(
        summary = "자체 DB 기준 CGM 데이터 페이징 조회",
        description = "매번 i-sens API를 직접 호출하지 않고, /api/cgm/sync로 미리 적재해둔 데이터를 조회한다. "
            + "sort 파라미터는 비워두거나 실제 필드명(예: eventAt,desc)만 입력할 것 — "
            + "Swagger 기본 예시값(\"string\")을 그대로 넣으면 정렬 필드 오류가 난다."
    )
    @GetMapping("/api/cgm/readings")
    public ResponseEntity<ApiResponse<Page<CgmReadingResponse>>> getReadings(
        @RequestParam String isensUserId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ParameterObject
        @Parameter(description = "sort는 비워두거나 eventAt,desc 같은 실제 필드명을 사용할 것")
        Pageable pageable
    ) {
        Page<CgmReadingResponse> readings = cgmReadingRepository
            .findByIsensUserIdAndEventAtBetweenOrderByEventAtDesc(isensUserId, start, end, pageable)
            .map(CgmReadingResponse::from);
        return ApiResponse.success(readings);
    }

    @Operation(
        summary = "[샌드박스 전용] 테스트 데이터 생성",
        description = "i-sens 샌드박스 전용 엔드포인트(/v1/public/samples) 호출 트리거. "
            + "운영 환경에는 존재하지 않으므로 샌드박스/개발 환경에서만 사용할 것."
    )
    @PostMapping("/api/cgm/sandbox/samples")
    public ResponseEntity<ApiResponse<SandboxSampleResponse>> generateSandboxSamples(
        @RequestParam String isensUserId
    ) {
        String accessToken = isensAuthService.getValidAccessToken(isensUserId);
        isensCgmApiClient.generateSandboxSamples(accessToken);
        return ApiResponse.success(SuccessStatus.CREATED, SandboxSampleResponse.completed());
    }
}
