package com.example.ddadang.domain.record.meal.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.meal.dto.request.MealRecordCreateRequest;
import com.example.ddadang.domain.record.meal.dto.response.MealRecordResponse;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.entity.MealRecord;
import com.example.ddadang.domain.record.meal.entity.MealRecordItem;
import com.example.ddadang.domain.record.meal.repository.MealRecordRepository;
import com.example.ddadang.domain.record.meal.status.MealErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
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
    public MealRecordResponse create(Long memberId, MealRecordCreateRequest request) {
        String memo = request.memo() == null || request.memo().isBlank() ? null : request.memo().strip();
        MealRecord record = new MealRecord(memberRepository.getReferenceById(memberId), request.eatenAt(), memo);

        for (MealRecordCreateRequest.Item item : request.items()) {
            Food food = foodService.getVisibleFood(memberId, item.foodId());
            if (!item.unit().matches(food.getServingUnit())) {
                throw new GeneralException(MealErrorStatus.INVALID_INTAKE_UNIT);
            }
            record.addItem(MealRecordItem.of(record, food, item.amount(), item.unit()));
        }

        return MealRecordResponse.from(mealRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MealRecordResponse get(Long memberId, Long mealRecordId) {
        MealRecord record = mealRecordRepository.findWithItemsById(mealRecordId)
            .filter(found -> found.isOwnedBy(memberId))
            .orElseThrow(() -> new GeneralException(MealErrorStatus.MEAL_RECORD_NOT_FOUND));
        return MealRecordResponse.from(record);
    }
}
