package com.example.ddadang.domain.record.insulin.init;

import static com.example.ddadang.domain.record.insulin.enums.InsulinActionType.INTERMEDIATE;
import static com.example.ddadang.domain.record.insulin.enums.InsulinActionType.LONG;
import static com.example.ddadang.domain.record.insulin.enums.InsulinActionType.MIXED;
import static com.example.ddadang.domain.record.insulin.enums.InsulinActionType.RAPID;
import static com.example.ddadang.domain.record.insulin.enums.InsulinActionType.SHORT;

import com.example.ddadang.domain.record.insulin.entity.InsulinProduct;
import com.example.ddadang.domain.record.insulin.repository.InsulinProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인슐린 제품 기본 목록(국내 주요 펜형 제품)을 적재한다. 제품이 하나라도 있으면 아무것도 하지 않는다.
 * TODO: 기획에서 확정 목록을 받으면 교체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InsulinProductDataInitializer implements ApplicationRunner {

    private final InsulinProductRepository insulinProductRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (insulinProductRepository.count() > 0) {
            return;
        }
        List<InsulinProduct> products = List.of(
            new InsulinProduct("노보래피드 플렉스펜", RAPID),
            new InsulinProduct("피아스프 플렉스터치", RAPID),
            new InsulinProduct("휴마로그 퀵펜", RAPID),
            new InsulinProduct("애피드라 솔로스타", RAPID),
            new InsulinProduct("휴물린 R", SHORT),
            new InsulinProduct("휴물린 N 퀵펜", INTERMEDIATE),
            new InsulinProduct("란투스 솔로스타", LONG),
            new InsulinProduct("투제오 솔로스타", LONG),
            new InsulinProduct("트레시바 플렉스터치", LONG),
            new InsulinProduct("레버미어 플렉스펜", LONG),
            new InsulinProduct("베이사글라 퀵펜", LONG),
            new InsulinProduct("노보믹스 30 플렉스펜", MIXED),
            new InsulinProduct("휴마로그 믹스 25 퀵펜", MIXED),
            new InsulinProduct("휴마로그 믹스 50 퀵펜", MIXED),
            new InsulinProduct("리조덱 플렉스터치", MIXED)
        );
        insulinProductRepository.saveAll(products);
        log.info("인슐린 제품 기본 데이터 {}건을 적재했습니다.", products.size());
    }
}
