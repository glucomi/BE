package com.example.ddadang.domain.cgm.entity;

import com.example.ddadang.domain.cgm.dto.response.CgmSampleResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * i-sens CGM API에서 받아온 원시 혈당 데이터를 자체 DB에 적재한 레코드.
 *
 * <p>필드 의미(doc116 확인됨):
 * <ul>
 *   <li>stage: 1=스무딩 진행 중(동일 serial_number+seq_number 값이 최대 6회까지 미세 조정될 수 있음,
 *       stage=2가 될 때까지 {@link #updateFrom}으로 덮어써야 함), 2=스무딩 완료(최종 확정)</li>
 *   <li>trend: 0=Unknown, 1=빠르게 감소, 2=감소, 3=서서히 감소, 4=안정적, 5=서서히 증가, 6=증가, 7=빠르게 증가</li>
 *   <li>min_max_flag: 0=정상범위(40~500), 1=40 미만, 2=500 초과</li>
 *   <li>error_code: 상세 enum 매핑표 아직 미확인, raw int로 보관</li>
 * </ul>
 */
@Entity
@Table(
    name = "cgm_reading",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cgm_reading_user_serial_seq",
        columnNames = {"isens_user_id", "serial_number", "seq_number"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CgmReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isens_user_id", nullable = false, length = 100)
    private String isensUserId;

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "seq_number", nullable = false)
    private Long seqNumber;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt;

    @Column(name = "tz_offset")
    private Integer tzOffset;

    @Column(name = "stage")
    private Integer stage;

    @Column(name = "initial_value")
    private Double initialValue;

    @Column(name = "value")
    private Double value;

    @Column(name = "trend_rate")
    private Double trendRate;

    @Column(name = "trend")
    private Integer trend;

    @Column(name = "error_code")
    private Integer errorCode;

    @Column(name = "min_max_flag")
    private Integer minMaxFlag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static CgmReading of(String isensUserId, CgmSampleResponse sample) {
        CgmReading reading = new CgmReading();
        reading.isensUserId = isensUserId;
        reading.serialNumber = sample.serialNumber();
        reading.seqNumber = sample.seqNumber();
        LocalDateTime now = LocalDateTime.now();
        reading.createdAt = now;
        reading.updatedAt = now;
        reading.applyValues(sample);
        return reading;
    }

    /**
     * stage=1(스무딩 진행 중) 구간에서는 동일 serial_number+seq_number로 최대 6회까지 값이
     * 미세 조정되어 재수신될 수 있다. stage=2(확정)에 도달하기 전까지는 최신 값으로 덮어쓴다.
     */
    public void updateFrom(CgmSampleResponse sample) {
        applyValues(sample);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isFinalized() {
        return this.stage != null && this.stage == 2;
    }

    private void applyValues(CgmSampleResponse sample) {
        this.eventAt = sample.eventAt();
        this.tzOffset = sample.tzOffset();
        this.stage = sample.stage();
        this.initialValue = sample.initialValue();
        this.value = sample.value();
        this.trendRate = sample.trendRate();
        this.trend = sample.trend();
        this.errorCode = sample.errorCode();
        this.minMaxFlag = sample.minMaxFlag();
    }
}
