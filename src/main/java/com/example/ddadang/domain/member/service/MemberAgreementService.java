package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.dto.request.AgreementRequest;
import com.example.ddadang.domain.member.dto.response.SignupStatusResponse;
import com.example.ddadang.domain.member.dto.response.TermsResponse;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.entity.MemberAgreement;
import com.example.ddadang.domain.member.enums.TermsType;
import com.example.ddadang.domain.member.repository.MemberAgreementRepository;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.member.status.MemberErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAgreementService {

    private final MemberRepository memberRepository;
    private final MemberAgreementRepository memberAgreementRepository;

    public List<TermsResponse> getTerms() {
        return Arrays.stream(TermsType.values()).map(TermsResponse::from).toList();
    }

    @Transactional
    public SignupStatusResponse agree(Long memberId, AgreementRequest request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));

        Map<TermsType, Boolean> agreedByType = toMap(request.agreements());
        boolean allRequiredAgreed = Arrays.stream(TermsType.values())
            .filter(TermsType::isRequired)
            .allMatch(type -> Boolean.TRUE.equals(agreedByType.get(type)));
        if (!allRequiredAgreed) {
            throw new GeneralException(MemberErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }

        LocalDateTime now = LocalDateTime.now();
        memberAgreementRepository.saveAll(agreedByType.entrySet().stream()
            .map(entry -> new MemberAgreement(member, entry.getKey(), entry.getValue(), now))
            .toList());
        member.completeSignup(Boolean.TRUE.equals(agreedByType.get(TermsType.MARKETING)));

        return SignupStatusResponse.from(member);
    }

    private Map<TermsType, Boolean> toMap(List<AgreementRequest.Item> items) {
        try {
            return items.stream().collect(Collectors.toMap(AgreementRequest.Item::termsType, AgreementRequest.Item::agreed));
        } catch (IllegalStateException e) {
            throw new GeneralException(MemberErrorStatus.DUPLICATED_TERMS);
        }
    }
}
