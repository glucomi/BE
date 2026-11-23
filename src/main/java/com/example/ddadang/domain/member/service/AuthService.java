package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.client.KakaoApiClient;
import com.example.ddadang.domain.member.client.KakaoUserResponse;
import com.example.ddadang.domain.member.dto.response.LoginResponse;
import com.example.ddadang.domain.member.dto.response.TokenResponse;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.entity.RefreshToken;
import com.example.ddadang.domain.member.enums.SocialProvider;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.member.repository.RefreshTokenRepository;
import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import com.example.ddadang.global.security.jwt.JwtProvider;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final Clock clock;

    /** 탈퇴 후 같은 소셜 계정으로 재가입할 수 없는 기간 */
    static final Duration REJOIN_RESTRICTION = Duration.ofDays(30);

    @Transactional
    public LoginResponse loginWithKakao(String kakaoAccessToken) {
        KakaoUserResponse kakaoUser = kakaoApiClient.getUser(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.id());

        Optional<Member> existing = memberRepository.findByProviderAndProviderId(SocialProvider.KAKAO, providerId)
            .filter(found -> !releaseIfRejoinable(found));
        Member member = existing.orElseGet(() -> memberRepository.save(
            Member.createSocialMember(SocialProvider.KAKAO, providerId, kakaoUser.nickname())
        ));

        return LoginResponse.of(member, issueTokens(member.getId()), existing.isEmpty());
    }

    /**
     * 웹 카카오 로그인: 인가 코드를 카카오 access token으로 교환한 뒤 앱 로그인과 같은 흐름을 탄다.
     */
    @Transactional
    public LoginResponse loginWithKakaoCode(String code, String redirectUri) {
        return loginWithKakao(kakaoApiClient.exchangeCode(code, redirectUri).accessToken());
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        Long memberId;
        try {
            memberId = jwtProvider.parseRefreshToken(refreshToken);
        } catch (GeneralException e) {
            throw new GeneralException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        RefreshToken saved = refreshTokenRepository.findByMemberId(memberId)
            .filter(token -> token.matches(refreshToken))
            .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_REFRESH_TOKEN));
        if (saved.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new GeneralException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        return issueTokens(memberId);
    }

    /**
     * 탈퇴 회원이면 30일이 지났는지 본다. 지났으면 기존 회원의 provider_id를 비우고 true(새 회원으로 가입),
     * 안 지났으면 403 AUTH_4031. 탈퇴 회원이 아니면 false(기존 회원 로그인).
     */
    private boolean releaseIfRejoinable(Member member) {
        if (!member.isWithdrawn()) {
            return false;
        }
        if (!member.canRejoin(LocalDateTime.now(clock), REJOIN_RESTRICTION)) {
            throw new GeneralException(AuthErrorStatus.REJOIN_RESTRICTED);
        }
        member.releaseProviderId();
        memberRepository.flush(); // 새 회원 insert보다 유니크 제약 해제(update)가 먼저 반영되도록
        return true;
    }

    private TokenResponse issueTokens(Long memberId) {
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        LocalDateTime expiresAt = jwtProvider.refreshTokenExpiresAt();

        refreshTokenRepository.findByMemberId(memberId)
            .ifPresentOrElse(
                saved -> saved.rotate(refreshToken, expiresAt),
                () -> refreshTokenRepository.save(new RefreshToken(memberId, refreshToken, expiresAt))
            );
        return new TokenResponse(accessToken, refreshToken);
    }
}
