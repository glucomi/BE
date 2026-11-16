package com.example.ddadang.domain.record.exercise.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.exercise.dto.request.ExerciseRecordRequest;
import com.example.ddadang.domain.record.exercise.dto.response.ExerciseRecordResponse;
import com.example.ddadang.domain.record.exercise.entity.Exercise;
import com.example.ddadang.domain.record.exercise.entity.ExerciseRecord;
import com.example.ddadang.domain.record.exercise.repository.ExerciseRecordRepository;
import com.example.ddadang.domain.record.exercise.status.ExerciseErrorStatus;
import com.example.ddadang.domain.record.weight.entity.WeightRecord;
import com.example.ddadang.domain.record.weight.repository.WeightRecordRepository;
import com.example.ddadang.global.exception.GeneralException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseRecordService {

    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseService exerciseService;
    private final WeightRecordRepository weightRecordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ExerciseRecordResponse create(Long memberId, ExerciseRecordRequest request) {
        Exercise exercise = exerciseService.getExercise(request.exerciseId());
        ExerciseRecord record = new ExerciseRecord(
            memberRepository.getReferenceById(memberId), exercise, request.performedAt(), request.durationMin(),
            weightAt(memberId, request.performedAt()), memo(request)
        );
        return ExerciseRecordResponse.from(exerciseRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ExerciseRecordResponse> getByDate(Long memberId, LocalDate date) {
        return exerciseRecordRepository
            .findByMemberIdAndPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(ExerciseRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseRecordResponse get(Long memberId, Long exerciseRecordId) {
        return ExerciseRecordResponse.from(getOwned(memberId, exerciseRecordId));
    }

    /**
     * 운동/일시/시간이 바뀔 수 있으므로 소모 열량을 다시 계산한다.
     */
    @Transactional
    public ExerciseRecordResponse update(Long memberId, Long exerciseRecordId, ExerciseRecordRequest request) {
        ExerciseRecord record = getOwned(memberId, exerciseRecordId);
        record.update(
            exerciseService.getExercise(request.exerciseId()), request.performedAt(), request.durationMin(),
            weightAt(memberId, request.performedAt()), memo(request)
        );
        return ExerciseRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long exerciseRecordId) {
        exerciseRecordRepository.delete(getOwned(memberId, exerciseRecordId));
    }

    /**
     * 운동 일시 이전 가장 최근 체중. 그 이전 기록이 없으면 가장 오래된(운동 일시와 가장 가까운) 체중, 체중 기록이 없으면 null.
     */
    private BigDecimal weightAt(Long memberId, LocalDateTime performedAt) {
        return weightRecordRepository
            .findFirstByMemberIdAndMeasuredAtLessThanEqualOrderByMeasuredAtDesc(memberId, performedAt)
            .or(() -> weightRecordRepository.findFirstByMemberIdOrderByMeasuredAtAsc(memberId))
            .map(WeightRecord::getWeightKg)
            .orElse(null);
    }

    private ExerciseRecord getOwned(Long memberId, Long exerciseRecordId) {
        return exerciseRecordRepository.findWithExerciseById(exerciseRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(ExerciseErrorStatus.EXERCISE_RECORD_NOT_FOUND));
    }

    private String memo(ExerciseRecordRequest request) {
        return request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
    }
}
