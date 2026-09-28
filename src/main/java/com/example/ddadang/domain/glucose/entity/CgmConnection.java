package com.example.ddadang.domain.glucose.entity;

import com.example.ddadang.domain.glucose.enums.CgmConnectionStatus;
import com.example.ddadang.domain.glucose.enums.CgmProvider;
import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원과 CGM 제공사 계정의 연결. 케어센스 에어는 i-sens OAuth 토큰을 함께 보관한다.
 * 같은 제공사 계정(externalUserId)은 한 회원에게만 연결될 수 있다.
 */
@Entity
@Table(
    name = "cgm_connection",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cgm_connection_provider_user",
        columnNames = {"provider", "external_user_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CgmConnection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private CgmProvider provider;

    @Column(name = "external_user_id", nullable = false, length = 100)
    private String externalUserId;

    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "sensor_serial", length = 100)
    private String sensorSerial;

    @Column(name = "sensor_started_at")
    private OffsetDateTime sensorStartedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CgmConnectionStatus status;

    public static CgmConnection connect(
        Member member, CgmProvider provider, String externalUserId,
        String accessToken, String refreshToken, LocalDateTime tokenExpiresAt
    ) {
        CgmConnection connection = new CgmConnection();
        connection.member = member;
        connection.provider = provider;
        connection.externalUserId = externalUserId;
        connection.updateTokens(accessToken, refreshToken, tokenExpiresAt);
        connection.status = CgmConnectionStatus.CONNECTED;
        return connection;
    }

    public void reconnect(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        updateTokens(accessToken, refreshToken, tokenExpiresAt);
        this.status = CgmConnectionStatus.CONNECTED;
    }

    public void updateTokens(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        this.accessToken = accessToken;
        if (refreshToken != null) {
            this.refreshToken = refreshToken;
        }
        this.tokenExpiresAt = tokenExpiresAt;
    }

    /**
     * 센서 정보 API 경로가 미확인이라, 동기화된 혈당 데이터의 시리얼이 바뀌면 새 센서로 보고
     * 해당 시리얼의 첫 측정 시각을 부착 시각으로 기록한다(N일차 계산용).
     */
    public void updateSensor(String serialNumber, OffsetDateTime firstMeasuredAt) {
        if (Objects.equals(this.sensorSerial, serialNumber)) {
            return;
        }
        this.sensorSerial = serialNumber;
        this.sensorStartedAt = firstMeasuredAt;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean isTokenExpired() {
        return tokenExpiresAt == null || LocalDateTime.now().isAfter(tokenExpiresAt);
    }
}
