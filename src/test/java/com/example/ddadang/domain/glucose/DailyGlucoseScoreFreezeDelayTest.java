package com.example.ddadang.domain.glucose;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddadang.domain.glucose.service.DailyGlucoseScoreService;
import com.example.ddadang.domain.member.client.KakaoApiClient;
import com.example.ddadang.support.FixedClockConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 확정 유예 11시간 설정 시, 9/29 10:00에는 9/28이 아직 확정 대상이 아니다.
 */
@SpringBootTest(properties = "glucose.analysis.freeze.delay=11h")
@Import(FixedClockConfig.class)
class DailyGlucoseScoreFreezeDelayTest {

    @Autowired
    private DailyGlucoseScoreService dailyGlucoseScoreService;

    @MockitoBean
    private KakaoApiClient kakaoApiClient;

    @Test
    void 유예시간이_지나지_않은_날은_확정하지_않는다() {
        assertThat(dailyGlucoseScoreService.latestFreezableDate()).isEqualTo(LocalDate.of(2026, 9, 27));
    }
}
