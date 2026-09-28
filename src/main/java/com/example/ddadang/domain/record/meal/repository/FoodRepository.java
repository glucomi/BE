package com.example.ddadang.domain.record.meal.repository;

import com.example.ddadang.domain.record.meal.entity.Food;
import com.example.ddadang.domain.record.meal.enums.FoodSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * DB 음식 + 본인이 직접 등록한 음식 중 음식명/브랜드명에 키워드가 포함된 것. 이름이 짧을수록(정확도 높을수록) 먼저.
     */
    @Query("""
        select f from Food f
        where (f.source = com.example.ddadang.domain.record.meal.enums.FoodSource.DB or f.member.id = :memberId)
          and (lower(f.name) like lower(concat('%', :keyword, '%'))
               or lower(f.brand) like lower(concat('%', :keyword, '%')))
        order by length(f.name), f.name
        """)
    Page<Food> search(@Param("memberId") Long memberId, @Param("keyword") String keyword, Pageable pageable);

    Page<Food> findByMemberIdAndSourceOrderByCreatedAtDesc(Long memberId, FoodSource source, Pageable pageable);

    boolean existsBySource(FoodSource source);
}
