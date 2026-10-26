package com.example.ddadang.domain.record.weight.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.weight.dto.request.WeightRecordRequest;
import com.example.ddadang.domain.record.weight.dto.response.WeightRecordResponse;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.domain.record.weight.status.WeightRecordErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 체중은 하루 1건(MO-WEIGHT-010: "동일 일자에 기존 기록이 있으면 최신값으로 갱신").
 */
@Service
@RequiredArgsConstructor
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;
    private final MemberRepository memberRepository;

    /**
     * 해당 날짜 기록이 있으면 체중을 갱신하고, 없으면 새로 저장한다.
     */
    @Transactional
    public WeightRecordResponse save(Long memberId, LocalDate date, WeightRecordRequest request) {
        WeightRecord record = findByDate(memberId, date)
            .map(existing -> {
                existing.changeWeight(request.weightKg());
                return existing;
            })
            .orElseGet(() -> weightRecordRepository.save(new WeightRecord(
                memberRepository.getReferenceById(memberId), date.atStartOfDay(), request.weightKg()
            )));
        return WeightRecordResponse.from(record);
    }

    /**
     * 해당 날짜 기록. 없으면 null.
     */
    @Transactional(readOnly = true)
    public WeightRecordResponse getByDate(Long memberId, LocalDate date) {
        return findByDate(memberId, date).map(WeightRecordResponse::from).orElse(null);
    }

    /**
     * 가장 최근 체중(체중 입력 화면 기본값). 없으면 null.
     */
    @Transactional(readOnly = true)
    public WeightRecordResponse getLatest(Long memberId) {
        return weightRecordRepository.findFirstByMemberIdOrderByMeasuredAtDesc(memberId)
            .map(WeightRecordResponse::from)
            .orElse(null);
    }

    @Transactional
    public void delete(Long memberId, LocalDate date) {
        List<WeightRecord> records = recordsOf(memberId, date);
        if (records.isEmpty()) {
            throw new GeneralException(WeightRecordErrorStatus.WEIGHT_RECORD_NOT_FOUND);
        }
        weightRecordRepository.deleteAll(records);
    }

    private Optional<WeightRecord> findByDate(Long memberId, LocalDate date) {
        return recordsOf(memberId, date).stream().findFirst();
    }

    /**
     * 해당 날짜 기록(최신순). 정상적으로는 1건이지만 이전 데이터가 여러 건이어도 최신 1건을 기준으로 쓴다.
     */
    private List<WeightRecord> recordsOf(Long memberId, LocalDate date) {
        return weightRecordRepository
            .findByMemberIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtDesc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            );
    }
}
