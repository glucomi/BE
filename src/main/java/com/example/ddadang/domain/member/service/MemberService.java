package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.dto.request.OnboardingRequest;
import com.example.ddadang.domain.member.dto.request.WithdrawalRequest;
import com.example.ddadang.domain.member.dto.response.MemberResponse;
import com.example.ddadang.domain.member.dto.response.NotificationSettingsResponse;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.entity.MemberWithdrawal;
import com.example.ddadang.domain.member.event.OnboardingCompletedEvent;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.member.repository.MemberWithdrawalRepository;
import com.example.ddadang.domain.member.repository.RefreshTokenRepository;
import com.example.ddadang.domain.member.status.AuthErrorStatus;
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
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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

    @Transactional(readOnly = true)
    public NotificationSettingsResponse getNotificationSettings(Long memberId) {
        return NotificationSettingsResponse.from(findMember(memberId));
    }

    /**
     * 회원을 탈퇴 상태로 바꾸고 refresh token을 삭제한다(재발급 불가). 기록 데이터는 바로 지우지 않는다.
     * 이미 발급된 access token은 만료(최대 1시간) 전까지 유효하다.
     */
    @Transactional
    public void withdraw(Long memberId, WithdrawalRequest request) {
        Member member = findMember(memberId);
        if (member.isWithdrawn()) {
            throw new GeneralException(AuthErrorStatus.WITHDRAWN_MEMBER);
        }
        member.withdraw(LocalDateTime.now());
        memberWithdrawalRepository.save(new MemberWithdrawal(memberId, request.reason()));
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
}
