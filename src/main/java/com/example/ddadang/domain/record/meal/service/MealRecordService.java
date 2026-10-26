package com.example.ddadang.domain.record.meal.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.meal.dto.request.MealRecordRequest;
import com.example.ddadang.domain.record.meal.dto.response.MealRecordResponse;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.MealRecord;
import com.example.ddadang.domain.record.meal.entity.MealRecordItem;
import com.example.ddadang.domain.record.meal.repository.MealRecordRepository;
import com.example.ddadang.domain.record.meal.status.MealErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealRecordService {

    private final MealRecordRepository mealRecordRepository;
    private final MemberRepository memberRepository;
    private final FoodService foodService;

    @Transactional
    public MealRecordResponse create(Long memberId, MealRecordRequest request) {
        MealRecord record = new MealRecord(memberRepository.getReferenceById(memberId), request.eatenAt(), memo(request));
        addItems(memberId, record, request.items());
        return MealRecordResponse.from(mealRecordRepository.save(record));
    }

    /**
     * 식사 일시/메모/메뉴 전체를 요청값으로 교체한다. 영양 성분은 수정 시점의 음식 정보로 다시 환산한다.
     */
    @Transactional
    public MealRecordResponse update(Long memberId, Long mealRecordId, MealRecordRequest request) {
        MealRecord record = getOwned(memberId, mealRecordId);
        record.update(request.eatenAt(), memo(request));
        addItems(memberId, record, request.items());
        mealRecordRepository.flush(); // 새 메뉴 ID를 응답에 담기 위해 즉시 반영
        return MealRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long memberId, Long mealRecordId) {
        mealRecordRepository.delete(getOwned(memberId, mealRecordId));
    }

    /**
     * [from, to) 구간의 식사 기록을 시간순으로 조회한다(홈 화면 등 다른 도메인 조회용).
     */
    @Transactional(readOnly = true)
    public List<MealRecordResponse> getMealRecords(Long memberId, LocalDateTime from, LocalDateTime to) {
        return mealRecordRepository
            .findByMemberIdAndEatenAtGreaterThanEqualAndEatenAtLessThanOrderByEatenAtAsc(memberId, from, to)
            .stream()
            .map(MealRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public MealRecordResponse get(Long memberId, Long mealRecordId) {
        return MealRecordResponse.from(getOwned(memberId, mealRecordId));
    }

    private MealRecord getOwned(Long memberId, Long mealRecordId) {
        return mealRecordRepository.findWithItemsById(mealRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(MealErrorStatus.MEAL_RECORD_NOT_FOUND));
    }

    private void addItems(Long memberId, MealRecord record, List<MealRecordRequest.Item> items) {
        for (MealRecordRequest.Item item : items) {
            Food food = foodService.getVisibleFood(memberId, item.foodId());
            if (!item.unit().matches(food.getServingUnit())) {
                throw new GeneralException(MealErrorStatus.INVALID_INTAKE_UNIT);
            }
            record.addItem(MealRecordItem.of(record, food, item.amount(), item.unit()));
        }
    }

    private String memo(MealRecordRequest request) {
        return request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
    }
}
