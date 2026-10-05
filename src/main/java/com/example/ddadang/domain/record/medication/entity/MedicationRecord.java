package com.example.ddadang.domain.record.medication.entity;

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

@Entity
@Table(
    name = "medication_record",
    indexes = @Index(name = "idx_medication_record_member_taken_at", columnList = "member_id, taken_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicationRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_medication_id", nullable = false)
    private MemberMedication memberMedication;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "memo", length = 1000)
    private String memo;

    public MedicationRecord(Member member, MemberMedication memberMedication, LocalDateTime takenAt, String memo) {
        this.member = member;
        this.memberMedication = memberMedication;
        this.takenAt = takenAt;
        this.memo = memo;
    }

    public void update(MemberMedication memberMedication, LocalDateTime takenAt, String memo) {
        this.memberMedication = memberMedication;
        this.takenAt = takenAt;
        this.memo = memo;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }
}
