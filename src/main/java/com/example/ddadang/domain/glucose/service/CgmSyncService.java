package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.dto.response.CgmSampleResponse;
import com.example.ddadang.domain.glucose.dto.response.CgmSyncResultResponse;
import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.repository.CgmReadingRepository;
import com.example.ddadang.domain.glucose.util.CgmDateRangeSplitter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    public CgmSyncResultResponse syncCgmData(Long memberId, OffsetDateTime start, OffsetDateTime end) {
        String accessToken = isensAuthService.getValidAccessToken(memberId);
        CgmConnection connection = isensAuthService.getConnection(memberId);

        List<CgmSampleResponse> fetched = CgmDateRangeSplitter.split(start, end).stream()
            .map(range -> isensCgmApiClient.fetchCgmData(accessToken, range.start(), range.end()))
            .flatMap(List::stream)
            .toList();

        int insertedCount = 0;
        int updatedCount = 0;
        for (var group : groupBySerial(fetched).entrySet()) {
            UpsertResult result = upsertReadings(connection, group.getKey(), group.getValue());
            insertedCount += result.inserted();
            updatedCount += result.updated();
        }
        updateLatestSensor(connection, fetched);

        return new CgmSyncResultResponse(fetched.size(), insertedCount, updatedCount);
    }

    private Map<String, List<CgmSampleResponse>> groupBySerial(List<CgmSampleResponse> samples) {
        return samples.stream().collect(Collectors.groupingBy(CgmSampleResponse::serialNumber));
    }

    private UpsertResult upsertReadings(CgmConnection connection, String serialNumber, List<CgmSampleResponse> samples) {
        List<Long> seqNumbers = samples.stream().map(CgmSampleResponse::seqNumber).toList();
        Map<Long, CgmReading> existingBySeqNumber = cgmReadingRepository
            .findByCgmConnectionIdAndSerialNumberAndSeqNumberIn(connection.getId(), serialNumber, seqNumbers)
            .stream()
            .collect(Collectors.toMap(CgmReading::getSeqNumber, Function.identity()));

        List<CgmReading> newReadings = new ArrayList<>();
        int updatedCount = 0;
        for (CgmSampleResponse sample : samples) {
            CgmReading existing = existingBySeqNumber.get(sample.seqNumber());
            if (existing == null) {
                newReadings.add(CgmReading.of(connection, sample));
            } else if (!existing.isFinalized()) {
                existing.updateFrom(sample);
                updatedCount++;
            }
        }

        cgmReadingRepository.saveAll(newReadings);
        return new UpsertResult(newReadings.size(), updatedCount);
    }

    private void updateLatestSensor(CgmConnection connection, List<CgmSampleResponse> fetched) {
        fetched.stream()
            .max(Comparator.comparing(CgmSampleResponse::eventAt))
            .map(CgmSampleResponse::serialNumber)
            .ifPresent(latestSerial -> fetched.stream()
                .filter(sample -> sample.serialNumber().equals(latestSerial))
                .map(CgmSampleResponse::eventAt)
                .min(Comparator.naturalOrder())
                .ifPresent(firstMeasuredAt -> connection.updateSensor(latestSerial, firstMeasuredAt)));
    }

    private record UpsertResult(int inserted, int updated) {
    }
}
