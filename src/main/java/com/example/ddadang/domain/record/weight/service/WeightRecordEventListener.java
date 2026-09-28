package com.example.ddadang.domain.record.weight.service;

import com.example.ddadang.domain.member.event.OnboardingCompletedEvent;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 온보딩 저장과 같은 트랜잭션에서 실행되도록 @EventListener(동기)를 사용한다.
 * 체중 저장이 실패하면 온보딩도 함께 롤백된다.
 */
@Component
@RequiredArgsConstructor
public class WeightRecordEventListener {

    private final MemberRepository memberRepository;
    private final WeightRecordRepository weightRecordRepository;

    @EventListener
    public void handleOnboardingCompleted(OnboardingCompletedEvent event) {
        weightRecordRepository.save(new WeightRecord(
            memberRepository.getReferenceById(event.memberId()),
            event.completedAt(),
            event.weightKg()
        ));
    }
}
