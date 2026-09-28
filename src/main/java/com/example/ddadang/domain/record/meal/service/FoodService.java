package com.example.ddadang.domain.record.meal.service;

import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.record.meal.dto.request.CustomFoodCreateRequest;
import com.example.ddadang.domain.record.meal.dto.response.FoodDetailResponse;
import com.example.ddadang.domain.record.meal.dto.response.FoodSummaryResponse;
import com.example.ddadang.domain.record.meal.entity.FavoriteFood;
import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import com.example.ddadang.domain.record.meal.repository.FavoriteFoodRepository;
import com.example.ddadang.domain.record.meal.repository.FoodRepository;
import com.example.ddadang.domain.record.meal.status.MealErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;
    private final FavoriteFoodRepository favoriteFoodRepository;
    private final MemberRepository memberRepository;

    public Page<FoodSummaryResponse> search(Long memberId, String keyword, Pageable pageable) {
        Page<Food> foods = foodRepository.search(memberId, keyword.strip(), pageable);
        return withFavorite(memberId, foods);
    }

    public FoodDetailResponse getDetail(Long memberId, Long foodId) {
        Food food = getVisibleFood(memberId, foodId);
        return FoodDetailResponse.of(food, favoriteFoodRepository.existsByMemberIdAndFoodId(memberId, foodId));
    }

    public Page<FoodSummaryResponse> getFavorites(Long memberId, Pageable pageable) {
        return favoriteFoodRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
            .map(favorite -> FoodSummaryResponse.of(favorite.getFood(), true));
    }

    /**
     * 이미 즐겨찾기한 음식이면 그대로 둔다(멱등).
     */
    @Transactional
    public void addFavorite(Long memberId, Long foodId) {
        Food food = getVisibleFood(memberId, foodId);
        if (!favoriteFoodRepository.existsByMemberIdAndFoodId(memberId, foodId)) {
            favoriteFoodRepository.save(new FavoriteFood(memberRepository.getReferenceById(memberId), food));
        }
    }

    @Transactional
    public void removeFavorite(Long memberId, Long foodId) {
        favoriteFoodRepository.findByMemberIdAndFoodId(memberId, foodId).ifPresent(favoriteFoodRepository::delete);
    }

    public Page<FoodSummaryResponse> getCustomFoods(Long memberId, Pageable pageable) {
        return withFavorite(memberId, foodRepository.findByMemberIdAndSourceOrderByCreatedAtDesc(
            memberId, FoodSource.CUSTOM, pageable
        ));
    }

    @Transactional
    public FoodDetailResponse createCustomFood(Long memberId, CustomFoodCreateRequest request) {
        Food food = foodRepository.save(Food.customFood(
            memberRepository.getReferenceById(memberId),
            request.name().strip(),
            request.brand() == null || request.brand().isBlank() ? null : request.brand().strip(),
            request.servingAmount(),
            request.servingUnit(),
            request.nutrients().toEntity()
        ));
        return FoodDetailResponse.of(food, false);
    }

    public Food getVisibleFood(Long memberId, Long foodId) {
        return foodRepository.findById(foodId)
            .filter(food -> food.isVisibleTo(memberId))
            .orElseThrow(() -> new GeneralException(MealErrorStatus.FOOD_NOT_FOUND));
    }

    private Page<FoodSummaryResponse> withFavorite(Long memberId, Page<Food> foods) {
        Set<Long> favoriteIds = foods.isEmpty()
            ? Set.of()
            : new HashSet<>(favoriteFoodRepository.findFavoriteFoodIds(
                memberId, foods.map(Food::getId).getContent()
            ));
        return foods.map(food -> FoodSummaryResponse.of(food, favoriteIds.contains(food.getId())));
    }
}
