package com.example.ddadang.domain.record.insulin.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.insulin.dto.request.InsulinRecordRequest;
import com.example.ddadang.domain.record.insulin.dto.response.InsulinRecordResponse;
import com.example.ddadang.domain.record.insulin.entity.InsulinRecord;
import com.example.ddadang.domain.record.insulin.entity.MemberInsulin;
import com.example.ddadang.domain.record.insulin.repository.InsulinRecordRepository;
import com.example.ddadang.domain.record.insulin.status.InsulinErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InsulinRecordService {

    private final InsulinRecordRepository insulinRecordRepository;
    private final InsulinService insulinService;
    private final MemberRepository memberRepository;

    @Transactional
    public InsulinRecordResponse create(Long memberId, InsulinRecordRequest request) {
        MemberInsulin memberInsulin = insulinService.getActive(memberId, request.memberInsulinId());
        InsulinRecord record = new InsulinRecord(
            memberRepository.getReferenceById(memberId), memberInsulin, request.injectedAt(), request.doseUnit(),
            memo(request)
        );
        return InsulinRecordResponse.from(insulinRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<InsulinRecordResponse> getByDate(Long memberId, LocalDate date) {
        return insulinRecordRepository
            .findByMemberIdAndInjectedAtGreaterThanEqualAndInjectedAtLessThanOrderByInjectedAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(InsulinRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public InsulinRecordResponse get(Long memberId, Long insulinRecordId) {
        return InsulinRecordResponse.from(getOwned(memberId, insulinRecordId));
    }

    /**
     * 내 인슐린을 바꿀 때는 삭제되지 않은 것만 고를 수 있다. 기존과 같으면(이미 삭제된 인슐린이어도) 그대로 둔다.
     */
    @Transactional
    public InsulinRecordResponse update(Long memberId, Long insulinRecordId, InsulinRecordRequest request) {
        InsulinRecord record = getOwned(memberId, insulinRecordId);
        MemberInsulin memberInsulin = record.getMemberInsulin().getId().equals(request.memberInsulinId())
            ? record.getMemberInsulin()
            : insulinService.getActive(memberId, request.memberInsulinId());
        record.update(memberInsulin, request.injectedAt(), request.doseUnit(), memo(request));
        return InsulinRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long insulinRecordId) {
        insulinRecordRepository.delete(getOwned(memberId, insulinRecordId));
    }

    private InsulinRecord getOwned(Long memberId, Long insulinRecordId) {
        return insulinRecordRepository.findWithInsulinById(insulinRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(InsulinErrorStatus.INSULIN_RECORD_NOT_FOUND));
    }

    private String memo(InsulinRecordRequest request) {
        return request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
    }
}
