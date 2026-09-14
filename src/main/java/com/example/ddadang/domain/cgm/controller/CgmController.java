package com.example.ddadang.domain.cgm.controller;

import com.example.ddadang.domain.cgm.dto.response.CgmReadingResponse;
import com.example.ddadang.domain.cgm.dto.response.CgmSyncResultResponse;
import com.example.ddadang.domain.cgm.repository.CgmReadingRepository;
import com.example.ddadang.domain.cgm.service.CgmSyncService;
import com.example.ddadang.domain.cgm.service.IsensAuthService;
import com.example.ddadang.domain.cgm.service.IsensCgmApiClient;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * isensUserId를 요청 파라미터로 직접 받는 것은 임시 조치이다.
 * TODO: Member 도메인/인증이 붙으면 로그인한 사용자 principal에서 isensUserId를 조회하도록 교체.
 */
@RestController
@RequiredArgsConstructor
public class CgmController {

    private final CgmSyncService cgmSyncService;
    private final CgmReadingRepository cgmReadingRepository;
    private final IsensAuthService isensAuthService;
    private final IsensCgmApiClient isensCgmApiClient;

    @PostMapping("/api/cgm/sync")
    public CgmSyncResultResponse sync(
        @RequestParam String isensUserId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        return cgmSyncService.syncCgmData(isensUserId, start, end);
    }

    /**
     * 자체 DB 기준 페이징 조회. 매번 i-sens API를 직접 호출하는 대신
     * /api/cgm/sync로 적재해둔 데이터를 조회한다.
     */
    @GetMapping("/api/cgm/readings")
    public Page<CgmReadingResponse> getReadings(
        @RequestParam String isensUserId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        Pageable pageable
    ) {
        return cgmReadingRepository
            .findByIsensUserIdAndEventAtBetweenOrderByEventAtDesc(isensUserId, start, end, pageable)
            .map(CgmReadingResponse::from);
    }

    /**
     * 샌드박스 전용 테스트 데이터 생성 트리거. 운영 환경에는 존재하지 않는 i-sens 엔드포인트이므로
     * 샌드박스/개발 환경에서만 사용할 것.
     */
    @PostMapping("/api/cgm/sandbox/samples")
    @ResponseStatus(HttpStatus.CREATED)
    public void generateSandboxSamples(@RequestParam String isensUserId) {
        String accessToken = isensAuthService.getValidAccessToken(isensUserId);
        isensCgmApiClient.generateSandboxSamples(accessToken);
    }
}
