package com.example.ddadang.domain.record.meal.repository;

import com.example.ddadang.domain.record.meal.entity.MealRecordItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealRecordItemRepository extends JpaRepository<MealRecordItem, Long> {

    /**
     * 회원이 먹은 음식 ID를 가장 최근에 먹은 순으로 중복 없이 조회한다.
     */
    @Query(
        value = """
            select i.food.id from MealRecordItem i
            where i.mealRecord.member.id = :memberId
            group by i.food.id
            order by max(i.mealRecord.eatenAt) desc
            """,
        countQuery = """
            select count(distinct i.food.id) from MealRecordItem i
            where i.mealRecord.member.id = :memberId
            """
    )
    Page<Long> findRecentFoodIds(@Param("memberId") Long memberId, Pageable pageable);
}
