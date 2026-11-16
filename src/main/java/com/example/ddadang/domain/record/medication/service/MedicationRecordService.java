package com.example.ddadang.domain.record.medication.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.medication.dto.request.MedicationRecordRequest;
import com.example.ddadang.domain.record.medication.dto.response.MedicationRecordResponse;
import com.example.ddadang.domain.record.medication.entity.MedicationRecord;
import com.example.ddadang.domain.record.medication.entity.MemberMedication;
import com.example.ddadang.domain.record.medication.repository.MedicationRecordRepository;
import com.example.ddadang.domain.record.medication.status.MedicationErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicationRecordService {

    private final MedicationRecordRepository medicationRecordRepository;
    private final MedicationService medicationService;
    private final MemberRepository memberRepository;

    @Transactional
    public MedicationRecordResponse create(Long memberId, MedicationRecordRequest request) {
        MemberMedication memberMedication = medicationService.getActive(memberId, request.memberMedicationId());
        MedicationRecord record = new MedicationRecord(
            memberRepository.getReferenceById(memberId), memberMedication, request.takenAt(), productName(request),
            memo(request)
        );
        return MedicationRecordResponse.from(medicationRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<MedicationRecordResponse> getByDate(Long memberId, LocalDate date) {
        return medicationRecordRepository
            .findByMemberIdAndTakenAtGreaterThanEqualAndTakenAtLessThanOrderByTakenAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(MedicationRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public MedicationRecordResponse get(Long memberId, Long medicationRecordId) {
        return MedicationRecordResponse.from(getOwned(memberId, medicationRecordId));
    }

    /**
     * 내 복용약을 바꿀 때는 삭제되지 않은 것만 고를 수 있다. 기존과 같으면(이미 삭제된 약이어도) 그대로 둔다.
     */
    @Transactional
    public MedicationRecordResponse update(Long memberId, Long medicationRecordId, MedicationRecordRequest request) {
        MedicationRecord record = getOwned(memberId, medicationRecordId);
        MemberMedication memberMedication = record.getMemberMedication().getId().equals(request.memberMedicationId())
            ? record.getMemberMedication()
            : medicationService.getActive(memberId, request.memberMedicationId());
        record.update(memberMedication, request.takenAt(), productName(request), memo(request));
        return MedicationRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long medicationRecordId) {
        medicationRecordRepository.delete(getOwned(memberId, medicationRecordId));
    }

    private MedicationRecord getOwned(Long memberId, Long medicationRecordId) {
        return medicationRecordRepository.findWithMedicationById(medicationRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(MedicationErrorStatus.MEDICATION_RECORD_NOT_FOUND));
    }

    private String productName(MedicationRecordRequest request) {
        return request.productName() == null || request.productName().isBlank() ? null : request.productName().strip();
    }

    private String memo(MedicationRecordRequest request) {
        return request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
    }
}
