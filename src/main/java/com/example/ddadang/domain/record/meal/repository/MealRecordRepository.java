package com.example.ddadang.domain.record.meal.repository;

import com.example.ddadang.domain.record.meal.entity.MealRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    @EntityGraph(attributePaths = {"items", "items.food"})
    Optional<MealRecord> findWithItemsById(Long id);
}
