package com.example.ddadang.domain.record.memo.entity;

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
 * 자유 메모 기록. 사진(memo_record_photo)은 스토리지 확정 후 추가한다.
 */
@Entity
@Table(
    name = "memo_record",
    indexes = @Index(name = "idx_memo_record_member_recorded_at", columnList = "member_id, recorded_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemoRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    public MemoRecord(Member member, LocalDateTime recordedAt, String content) {
        this.member = member;
        this.recordedAt = recordedAt;
        this.content = content;
    }

    public void update(LocalDateTime recordedAt, String content) {
        this.recordedAt = recordedAt;
        this.content = content;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
