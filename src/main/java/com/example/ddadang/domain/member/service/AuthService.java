package com.example.ddadang.domain.member.service;

import com.example.ddadang.domain.member.client.KakaoApiClient;
import com.example.ddadang.domain.member.client.KakaoUserResponse;
import com.example.ddadang.domain.member.dto.response.LoginResponse;
import com.example.ddadang.domain.member.dto.response.TokenResponse;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.domain.member.entity.RefreshToken;
import com.example.ddadang.domain.member.enums.MemberStatus;
import com.example.ddadang.domain.member.enums.SocialProvider;
import com.example.ddadang.domain.member.repository.MemberRepository;
import com.example.ddadang.domain.member.repository.RefreshTokenRepository;
import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import com.example.ddadang.global.security.jwt.JwtProvider;
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

    @Transactional
    public LoginResponse loginWithKakao(String kakaoAccessToken) {
        KakaoUserResponse kakaoUser = kakaoApiClient.getUser(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.id());

        Optional<Member> existing = memberRepository.findByProviderAndProviderId(SocialProvider.KAKAO, providerId);
        Member member = existing.orElseGet(() -> memberRepository.save(
            Member.createSocialMember(SocialProvider.KAKAO, providerId, kakaoUser.nickname())
        ));
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new GeneralException(AuthErrorStatus.WITHDRAWN_MEMBER);
        }

        return LoginResponse.of(member, issueTokens(member.getId()), existing.isEmpty());
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
