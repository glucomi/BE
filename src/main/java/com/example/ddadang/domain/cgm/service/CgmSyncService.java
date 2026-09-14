package com.example.ddadang.domain.cgm.service;

import com.example.ddadang.domain.cgm.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.cgm.dto.response.CgmSyncResultResponse;
import com.example.ddadang.domain.cgm.entity.CgmReading;
import com.example.ddadang.domain.cgm.repository.CgmReadingRepository;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter;
import com.example.ddadang.domain.cgm.util.CgmDateRangeSplitter.Range;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * i-sens CGM API에서 받아온 데이터를 자체 DB에 적재하는 동기화 로직.
 * 3개월 조회 제한 때문에 구간을 쪼개 호출한다.
 *
 * <p>stage=1(스무딩 진행 중)인 데이터는 동일 serial_number+seq_number로 최대 6회까지 값이
 * 미세 조정되어 재수신될 수 있으므로, 이미 저장된 레코드가 아직 확정(stage=2)되지 않았다면
 * 새 값으로 덮어쓴다. 확정된 레코드는 값이 더 이상 바뀌지 않으므로 건너뛴다.
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

        int insertedCount = 0;
        int updatedCount = 0;
        for (var group : groupBySerial(fetched).entrySet()) {
            UpsertResult result = upsertReadings(isensUserId, group.getKey(), group.getValue());
            insertedCount += result.inserted();
            updatedCount += result.updated();
        }

        return new CgmSyncResultResponse(fetched.size(), insertedCount, updatedCount);
    }

    private Map<String, List<CgmSampleResponse>> groupBySerial(List<CgmSampleResponse> samples) {
        return samples.stream().collect(Collectors.groupingBy(CgmSampleResponse::serialNumber));
    }

    private UpsertResult upsertReadings(String isensUserId, String serialNumber, List<CgmSampleResponse> samples) {
        List<Long> seqNumbers = samples.stream().map(CgmSampleResponse::seqNumber).toList();
        Map<Long, CgmReading> existingBySeqNumber = cgmReadingRepository
            .findByIsensUserIdAndSerialNumberAndSeqNumberIn(isensUserId, serialNumber, seqNumbers)
            .stream()
            .collect(Collectors.toMap(CgmReading::getSeqNumber, Function.identity()));

        List<CgmReading> newReadings = new ArrayList<>();
        int updatedCount = 0;
        for (CgmSampleResponse sample : samples) {
            CgmReading existing = existingBySeqNumber.get(sample.seqNumber());
            if (existing == null) {
                newReadings.add(CgmReading.of(isensUserId, sample));
            } else if (!existing.isFinalized()) {
                existing.updateFrom(sample);
                updatedCount++;
            }
        }

        cgmReadingRepository.saveAll(newReadings);
        return new UpsertResult(newReadings.size(), updatedCount);
    }

    private record UpsertResult(int inserted, int updated) {
    }
}
