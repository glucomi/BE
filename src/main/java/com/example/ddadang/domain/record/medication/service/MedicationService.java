package com.example.ddadang.domain.record.medication.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.medication.dto.request.MemberMedicationRequest;
import com.example.ddadang.domain.record.medication.dto.response.MemberMedicationResponse;
import com.example.ddadang.domain.record.medication.entity.MemberMedication;
import com.example.ddadang.domain.record.medication.repository.MemberMedicationRepository;
import com.example.ddadang.domain.record.medication.status.MedicationErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "내 복용약" 관리.
 */
@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MemberMedicationRepository memberMedicationRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<MemberMedicationResponse> getMyMedications(Long memberId) {
        return memberMedicationRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtAsc(memberId).stream()
            .map(MemberMedicationResponse::from)
            .toList();
    }

    @Transactional
    public MemberMedicationResponse addMyMedication(Long memberId, MemberMedicationRequest request) {
        MemberMedication medication = memberMedicationRepository.save(new MemberMedication(
            memberRepository.getReferenceById(memberId), request.category(), productName(request)
        ));
        return MemberMedicationResponse.from(medication);
    }

    @Transactional
    public MemberMedicationResponse updateMyMedication(
        Long memberId, Long memberMedicationId, MemberMedicationRequest request
    ) {
        MemberMedication medication = getActive(memberId, memberMedicationId);
        medication.update(request.category(), productName(request));
        return MemberMedicationResponse.from(medication);
    }

    /**
     * soft delete. 이미 남긴 복약 기록은 그대로 조회된다.
     */
    @Transactional
    public void deleteMyMedication(Long memberId, Long memberMedicationId) {
        getActive(memberId, memberMedicationId).delete(LocalDateTime.now(clock));
    }

    /**
     * 본인 소유이고 삭제되지 않은 내 복용약. 복약 기록 등록/수정 시 검증용.
     */
    @Transactional(readOnly = true)
    public MemberMedication getActive(Long memberId, Long memberMedicationId) {
        return memberMedicationRepository.findById(memberMedicationId)
            .filter(found -> found.isOwnedBy(memberId) && !found.isDeleted())
            .orElseThrow(() -> new GeneralException(MedicationErrorStatus.MEMBER_MEDICATION_NOT_FOUND));
    }

    private String productName(MemberMedicationRequest request) {
        return request.productName() == null || request.productName().isBlank() ? null : request.productName().strip();
    }
}
