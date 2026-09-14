package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.cgm.dto.response.CgmSyncResultResponse;
import com.example.ddadang.domain.cgm.entity.CgmReading;
import com.example.ddadang.domain.cgm.repository.CgmReadingRepository;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter.Range;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * i-sens CGM API에서 받아온 데이터를 자체 DB에 적재하는 동기화 로직.
 * 3개월 조회 제한 때문에 구간을 쪼개 호출하고, 시리얼+순번 기준으로 중복 저장을 막는다.
 */
@Service
@RequiredArgsConstructor
public class CgmSyncService {

    private final IsensAuthService isensAuthService;
    private final IsensCgmApiClient isensCgmApiClient;
    private final CgmReadingRepository cgmReadingRepository;

    @Transactional
    public CgmSyncResultResponse syncCgmData(String isensUserId, OffsetDateTime start, OffsetDateTime end) {
        String accessToken = isensAuthService.getValidAccessToken(isensUserId);

        List<CgmSampleResponse> fetched = CgmDateRangeSplitter.split(start, end).stream()
            .map(range -> isensCgmApiClient.fetchCgmData(accessToken, range.start(), range.end()))
            .flatMap(List::stream)
            .toList();

        int savedCount = 0;
        for (var group : groupBySerial(fetched).entrySet()) {
            savedCount += saveNewReadings(isensUserId, group.getKey(), group.getValue());
        }

        return new CgmSyncResultResponse(fetched.size(), savedCount);
    }

    private Map<String, List<CgmSampleResponse>> groupBySerial(List<CgmSampleResponse> samples) {
        return samples.stream().collect(Collectors.groupingBy(CgmSampleResponse::serialNumber));
    }

    private int saveNewReadings(String isensUserId, String serialNumber, List<CgmSampleResponse> samples) {
        List<Long> seqNumbers = samples.stream().map(CgmSampleResponse::seqNumber).toList();
        Set<Long> existingSeqNumbers = cgmReadingRepository
            .findByIsensUserIdAndSerialNumberAndSeqNumberIn(isensUserId, serialNumber, seqNumbers)
            .stream()
            .map(CgmReading::getSeqNumber)
            .collect(Collectors.toSet());

        List<CgmReading> newReadings = samples.stream()
            .filter(sample -> !existingSeqNumbers.contains(sample.seqNumber()))
            .map(sample -> CgmReading.of(isensUserId, sample))
            .toList();

        cgmReadingRepository.saveAll(newReadings);
        return newReadings.size();
    }
}
