package com.example.ddadang.domain.record.meal.repository;

import com.example.ddadang.domain.record.meal.entity.FavoriteFood;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteFoodRepository extends JpaRepository<FavoriteFood, Long> {

    @EntityGraph(attributePaths = "food")
    Page<FavoriteFood> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Optional<FavoriteFood> findByMemberIdAndFoodId(Long memberId, Long foodId);

    boolean existsByMemberIdAndFoodId(Long memberId, Long foodId);

    @Query("select ff.food.id from FavoriteFood ff where ff.member.id = :memberId and ff.food.id in :foodIds")
    List<Long> findFavoriteFoodIds(@Param("memberId") Long memberId, @Param("foodIds") Collection<Long> foodIds);
}
