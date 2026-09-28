package com.example.ddadang.domain.glucose.service;

import com.example.ddadang.domain.glucose.entity.CgmConnection;
import com.example.ddadang.domain.glucose.entity.CgmReading;
import com.example.ddadang.domain.glucose.enums.CgmConnectionStatus;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.glucose.repository.CgmConnectionRepository;
import com.example.ddadang.domain.glucose.repository.CgmReadingRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 다른 도메인(home)이 자체 DB에 적재된 혈당 데이터를 조회할 때 쓰는 읽기 전용 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlucoseQueryService {

    private final CgmReadingRepository cgmReadingRepository;
    private final CgmConnectionRepository cgmConnectionRepository;

    /**
     * [from, to) 구간의 혈당 값이 있는 측정치를 시간순으로 조회한다.
     */
    public List<CgmReading> getReadings(Long memberId, OffsetDateTime from, OffsetDateTime to) {
        return cgmReadingRepository
            .findByMemberIdAndEventAtGreaterThanEqualAndEventAtLessThanOrderByEventAtAsc(memberId, from, to)
            .stream()
            .filter(reading -> reading.getValue() != null)
            .toList();
    }

    public Optional<CgmConnection> findConnectedCgm(Long memberId) {
        return cgmConnectionRepository.findFirstByMemberIdAndProviderAndStatusOrderByUpdatedAtDesc(
            memberId, CgmProvider.CARESENS_AIR, CgmConnectionStatus.CONNECTED
        );
    }
}
