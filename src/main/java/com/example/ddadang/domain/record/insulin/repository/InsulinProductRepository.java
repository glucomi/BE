package com.example.ddadang.domain.record.insulin.repository;

import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsulinProductRepository extends JpaRepository<InsulinProduct, Long> {

    List<InsulinProduct> findByNameContaining(String keyword);
}
