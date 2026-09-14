package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.dto.response.CgmEventResponse;
import com.example.ddadang.domain.cgm.exception.InvalidEventTypeException;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter;
import com.example.ddadang.domain.cgm.util.CgmEventType;
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
 *
 * <p>i-sens 샌드박스 API에 event_type 쿼리 파라미터를 실측 전달해봐도(meal, 존재하지 않는 값 등)
 * 매번 전체 카테고리가 그대로 내려오는 것을 확인함 — 서버가 해당 파라미터를 무시하는 것으로 보임.
 * 그래서 event_type 필터는 응답을 받은 뒤 이 클래스에서 직접 걸러낸다.
 */
@Service
@RequiredArgsConstructor
public class CgmEventService {

    private final IsensAuthService isensAuthService;
    private final IsensCgmApiClient isensCgmApiClient;

    public Map<String, List<CgmEventResponse>> getEvents(
        String isensUserId, OffsetDateTime start, OffsetDateTime end, String eventType
    ) {
        if (eventType != null && !eventType.isBlank() && !CgmEventType.isValid(eventType)) {
            throw new InvalidEventTypeException();
        }

        String accessToken = isensAuthService.getValidAccessToken(isensUserId);

        Map<String, List<CgmEventResponse>> merged = new LinkedHashMap<>();
        for (var range : CgmDateRangeSplitter.split(start, end)) {
            Map<String, List<CgmEventResponse>> chunk =
                isensCgmApiClient.fetchEvents(accessToken, range.start(), range.end(), eventType);
            chunk.forEach((category, events) ->
                merged.computeIfAbsent(category, key -> new ArrayList<>()).addAll(events));
        }

        if (eventType == null || eventType.isBlank()) {
            return merged;
        }
        List<CgmEventResponse> filtered = merged.getOrDefault(eventType, List.of());
        return filtered.isEmpty() ? Map.of() : Map.of(eventType, filtered);
    }
}
