package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.dto.request.OnboardingRequest;
import com.example.ddadang.domain.member.dto.response.MemberResponse;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.event.OnboardingCompletedEvent;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.member.status.MemberErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    /**
     * 온보딩 재진입(다시 입력) 시에도 값을 덮어쓰고 체중 기록을 새로 남긴다.
     */
    @Transactional
    public MemberResponse completeOnboarding(Long memberId, OnboardingRequest request) {
        Member member = findMember(memberId);
        if (!member.isSignupCompleted()) {
            throw new GeneralException(MemberErrorStatus.SIGNUP_NOT_COMPLETED);
        }
        if (request.targetGlucoseMin() >= request.targetGlucoseMax()) {
            throw new GeneralException(MemberErrorStatus.INVALID_TARGET_GLUCOSE_RANGE);
        }

        member.completeOnboarding(
            request.diabetesType(), request.heightCm(), request.targetGlucoseMin(), request.targetGlucoseMax()
        );
        eventPublisher.publishEvent(new OnboardingCompletedEvent(memberId, request.weightKg(), LocalDateTime.now()));
        return MemberResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
}
