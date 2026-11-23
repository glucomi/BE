package com.example.ddadang.domain.record.memo.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.photo.service.PhotoService;
import com.example.ddadang.domain.record.memo.dto.request.MemoRecordRequest;
import com.example.ddadang.domain.record.memo.dto.response.MemoRecordResponse;
import com.example.ddadang.domain.record.memo.entity.MemoRecord;
import com.example.ddadang.domain.record.memo.repository.MemoRecordRepository;
import com.example.ddadang.domain.record.memo.status.MemoRecordErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemoRecordService {

    private final MemoRecordRepository memoRecordRepository;
    private final MemberRepository memberRepository;
    private final PhotoService photoService;

    @Transactional
    public MemoRecordResponse create(Long memberId, MemoRecordRequest request) {
        MemoRecord record = new MemoRecord(
            memberRepository.getReferenceById(memberId), request.recordedAt(), request.content().strip()
        );
        record.replacePhotos(photoService.validateOwned(memberId, request.photoIds()));
        return toResponse(memoRecordRepository.save(record));
    }

    /**
     * 해당 날짜 00:00~24:00 기록을 시간순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<MemoRecordResponse> getByDate(Long memberId, LocalDate date) {
        return memoRecordRepository
            .findByMemberIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                memberId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MemoRecordResponse get(Long memberId, Long memoRecordId) {
        return toResponse(getOwned(memberId, memoRecordId));
    }

    @Transactional
    public MemoRecordResponse update(Long memberId, Long memoRecordId, MemoRecordRequest request) {
        MemoRecord record = getOwned(memberId, memoRecordId);
        record.update(request.recordedAt(), request.content().strip());
        record.replacePhotos(photoService.validateOwned(memberId, request.photoIds()));
        return toResponse(record);
    }

    @Transactional
    public void delete(Long memberId, Long memoRecordId) {
        memoRecordRepository.delete(getOwned(memberId, memoRecordId));
    }

    private MemoRecordResponse toResponse(MemoRecord record) {
        return MemoRecordResponse.from(record, photoService.getPhotos(record.getPhotoIds()));
    }

    private MemoRecord getOwned(Long memberId, Long memoRecordId) {
        return memoRecordRepository.findById(memoRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(MemoRecordErrorStatus.MEMO_RECORD_NOT_FOUND));
    }
}
