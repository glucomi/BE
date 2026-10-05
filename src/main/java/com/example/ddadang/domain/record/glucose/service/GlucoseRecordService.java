package com.example.ddadang.domain.record.glucose.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.glucose.dto.request.GlucoseRecordRequest;
import com.example.ddadang.domain.record.glucose.dto.response.GlucoseRecordResponse;
import com.example.ddadang.domain.record.glucose.entity.GlucoseRecord;
import com.example.ddadang.domain.record.glucose.repository.GlucoseRecordRepository;
import com.example.ddadang.domain.record.glucose.status.GlucoseRecordErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GlucoseRecordService {

    private final GlucoseRecordRepository glucoseRecordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public GlucoseRecordResponse create(Long memberId, GlucoseRecordRequest request) {
        GlucoseRecord record = new GlucoseRecord(
            memberRepository.getReferenceById(memberId), request.measuredAt(), request.valueMgDl(), memo(request)
        );
        return GlucoseRecordResponse.from(glucoseRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<GlucoseRecordResponse> getByDate(Long memberId, LocalDate date) {
        return glucoseRecordRepository
            .findByMemberIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(GlucoseRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public GlucoseRecordResponse get(Long memberId, Long glucoseRecordId) {
        return GlucoseRecordResponse.from(getOwned(memberId, glucoseRecordId));
    }

    @Transactional
    public GlucoseRecordResponse update(Long memberId, Long glucoseRecordId, GlucoseRecordRequest request) {
        GlucoseRecord record = getOwned(memberId, glucoseRecordId);
        record.update(request.measuredAt(), request.valueMgDl(), memo(request));
        return GlucoseRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long glucoseRecordId) {
        glucoseRecordRepository.delete(getOwned(memberId, glucoseRecordId));
    }

    private GlucoseRecord getOwned(Long memberId, Long glucoseRecordId) {
        return glucoseRecordRepository.findById(glucoseRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(GlucoseRecordErrorStatus.GLUCOSE_RECORD_NOT_FOUND));
    }

    private String memo(GlucoseRecordRequest request) {
        return request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
    }
}
