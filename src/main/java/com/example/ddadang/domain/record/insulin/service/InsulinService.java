package com.example.ddadang.domain.record.insulin.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.insulin.dto.request.MemberInsulinCreateRequest;
import com.example.ddadang.domain.record.insulin.dto.request.MemberInsulinUpdateRequest;
import com.example.ddadang.domain.record.insulin.dto.response.InsulinProductResponse;
import com.example.ddadang.domain.record.insulin.dto.response.MemberInsulinResponse;
import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.entity.MemberInsulin;
import com.example.ddadang.domain.record.insulin.repository.InsulinProductRepository;
import com.example.ddadang.domain.record.insulin.repository.MemberInsulinRepository;
import com.example.ddadang.domain.record.insulin.status.InsulinErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인슐린 제품 목록과 "내 인슐린" 관리.
 */
@Service
@RequiredArgsConstructor
public class InsulinService {

    private final InsulinProductRepository insulinProductRepository;
    private final MemberInsulinRepository memberInsulinRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    /**
     * 작용 유형(초속효성 → 혼합형) → 제품명 순. keyword가 비어 있으면 전체 목록.
     * 작용 유형은 문자열로 저장돼 DB 정렬이 알파벳순이 되므로 enum 선언 순서로 메모리에서 정렬한다(제품 수가 적음).
     */
    @Transactional(readOnly = true)
    public List<InsulinProductResponse> getProducts(String keyword) {
        List<InsulinProduct> products = keyword == null || keyword.isBlank()
            ? insulinProductRepository.findAll()
            : insulinProductRepository.findByNameContaining(keyword.strip());
        return products.stream()
            .sorted(Comparator.comparing(InsulinProduct::getActionType).thenComparing(InsulinProduct::getName))
            .map(InsulinProductResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberInsulinResponse> getMyInsulins(Long memberId) {
        return memberInsulinRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtAsc(memberId).stream()
            .map(MemberInsulinResponse::from)
            .toList();
    }

    @Transactional
    public MemberInsulinResponse addMyInsulin(Long memberId, MemberInsulinCreateRequest request) {
        InsulinProduct product = insulinProductRepository.findById(request.insulinProductId())
            .orElseThrow(() -> new GeneralException(InsulinErrorStatus.INSULIN_PRODUCT_NOT_FOUND));
        if (memberInsulinRepository.existsByMemberIdAndInsulinProductIdAndDeletedAtIsNull(memberId, product.getId())) {
            throw new GeneralException(InsulinErrorStatus.MEMBER_INSULIN_ALREADY_EXISTS);
        }
        MemberInsulin memberInsulin = memberInsulinRepository.save(
            new MemberInsulin(memberRepository.getReferenceById(memberId), product, request.defaultDoseUnit())
        );
        return MemberInsulinResponse.from(memberInsulin);
    }

    @Transactional
    public MemberInsulinResponse updateMyInsulin(Long memberId, Long memberInsulinId, MemberInsulinUpdateRequest request) {
        MemberInsulin memberInsulin = getActive(memberId, memberInsulinId);
        memberInsulin.changeDefaultDose(request.defaultDoseUnit());
        return MemberInsulinResponse.from(memberInsulin);
    }

    /**
     * soft delete. 이미 남긴 인슐린 기록은 그대로 조회된다.
     */
    @Transactional
    public void deleteMyInsulin(Long memberId, Long memberInsulinId) {
        getActive(memberId, memberInsulinId).delete(LocalDateTime.now(clock));
    }

    /**
     * 본인 소유이고 삭제되지 않은 내 인슐린. 인슐린 기록 등록/수정 시 검증용.
     */
    @Transactional(readOnly = true)
    public MemberInsulin getActive(Long memberId, Long memberInsulinId) {
        return memberInsulinRepository.findWithProductById(memberInsulinId)
            .filter(found -> found.isOwnedBy(memberId) && !found.isDeleted())
            .orElseThrow(() -> new GeneralException(InsulinErrorStatus.MEMBER_INSULIN_NOT_FOUND));
    }
}
