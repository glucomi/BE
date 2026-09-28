package com.example.ddadang.support;

import com.example.ddadang.domain.member.client.KakaoApiClient;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.enums.SocialProvider;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.global.security.jwt.JwtProvider;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * H2(MySQL 모드) + MockMvc 통합 테스트 베이스. 외부 카카오 API는 목으로 대체한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected JwtProvider jwtProvider;

    @MockitoBean
    protected KakaoApiClient kakaoApiClient;

    protected Member createMember() {
        return memberRepository.save(
            Member.createSocialMember(SocialProvider.KAKAO, UUID.randomUUID().toString(), "테스트회원")
        );
    }

    protected String bearer(Member member) {
        return "Bearer " + jwtProvider.createAccessToken(member.getId());
    }
}
