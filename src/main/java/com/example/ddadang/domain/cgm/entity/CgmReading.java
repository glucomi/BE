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
 * stage / trend / error_code / min_max_flag는 enum 매핑표가 미확인 상태라 raw int로 저장한다.
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

    public static CgmReading of(String isensUserId, CgmSampleResponse sample) {
        CgmReading reading = new CgmReading();
        reading.isensUserId = isensUserId;
        reading.serialNumber = sample.serialNumber();
        reading.seqNumber = sample.seqNumber();
        reading.eventAt = sample.eventAt();
        reading.tzOffset = sample.tzOffset();
        reading.stage = sample.stage();
        reading.initialValue = sample.initialValue();
        reading.value = sample.value();
        reading.trendRate = sample.trendRate();
        reading.trend = sample.trend();
        reading.errorCode = sample.errorCode();
        reading.minMaxFlag = sample.minMaxFlag();
        reading.createdAt = LocalDateTime.now();
        return reading;
    }
}
