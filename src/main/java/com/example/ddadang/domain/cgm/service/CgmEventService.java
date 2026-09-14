package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.dto.response.CgmEventResponse;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이벤트 데이터는 CGM 혈당 데이터에 비해 저빈도라 자체 DB 적재 없이 i-sens API를
 * 실시간으로 조회(passthrough)한다. 3개월 조회 제한만 CgmDateRangeSplitter로 우회한다.
 */
@Service
@RequiredArgsConstructor
public class CgmEventService {

    private final IsensAuthService isensAuthService;
    private final IsensCgmApiClient isensCgmApiClient;

    public Map<String, List<CgmEventResponse>> getEvents(
        String isensUserId, OffsetDateTime start, OffsetDateTime end, String eventType
    ) {
        String accessToken = isensAuthService.getValidAccessToken(isensUserId);

        Map<String, List<CgmEventResponse>> merged = new LinkedHashMap<>();
        for (var range : CgmDateRangeSplitter.split(start, end)) {
            Map<String, List<CgmEventResponse>> chunk =
                isensCgmApiClient.fetchEvents(accessToken, range.start(), range.end(), eventType);
            chunk.forEach((category, events) ->
                merged.computeIfAbsent(category, key -> new ArrayList<>()).addAll(events));
        }
        return merged;
    }
}
