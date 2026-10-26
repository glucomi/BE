package com.example.ddadang.domain.record.glucose.entity;

import com.example.ddadang.domain.member.entity.Member;
import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 혈당계(BGM)로 잰 값을 사용자가 직접 입력한 혈당 기록. CGM 측정값(cgm_reading)과는 별개로 저장한다.
 */
@Entity
@Table(
    name = "glucose_record",
    indexes = @Index(name = "idx_glucose_record_member_measured_at", columnList = "member_id, measured_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GlucoseRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "value_mg_dl", nullable = false)
    private Integer valueMgDl;

    @Column(name = "memo", length = 1000)
    private String memo;

    public GlucoseRecord(Member member, LocalDateTime measuredAt, Integer valueMgDl, String memo) {
        this.member = member;
        this.measuredAt = measuredAt;
        this.valueMgDl = valueMgDl;
        this.memo = memo;
    }

    public void update(LocalDateTime measuredAt, Integer valueMgDl, String memo) {
        this.measuredAt = measuredAt;
        this.valueMgDl = valueMgDl;
        this.memo = memo;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
