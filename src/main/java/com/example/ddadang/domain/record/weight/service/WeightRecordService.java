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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WeightRecordResponse create(Long memberId, WeightRecordRequest request) {
        WeightRecord record = new WeightRecord(
            memberRepository.getReferenceById(memberId), request.measuredAt(), request.weightKg()
        );
        return WeightRecordResponse.from(weightRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<WeightRecordResponse> getByDate(Long memberId, LocalDate date) {
        return weightRecordRepository
            .findByMemberIdAndMeasuredAtGreaterThanEqualAndMeasuredAtLessThanOrderByMeasuredAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(WeightRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public WeightRecordResponse get(Long memberId, Long weightRecordId) {
        return WeightRecordResponse.from(getOwned(memberId, weightRecordId));
    }

    @Transactional
    public WeightRecordResponse update(Long memberId, Long weightRecordId, WeightRecordRequest request) {
        WeightRecord record = getOwned(memberId, weightRecordId);
        record.update(request.measuredAt(), request.weightKg());
        return WeightRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long weightRecordId) {
        weightRecordRepository.delete(getOwned(memberId, weightRecordId));
    }

    private WeightRecord getOwned(Long memberId, Long weightRecordId) {
        return weightRecordRepository.findById(weightRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(WeightRecordErrorStatus.WEIGHT_RECORD_NOT_FOUND));
    }
}
