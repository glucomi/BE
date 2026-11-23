package com.example.ddadang.domain.photo.entity;

import com.example.ddadang.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업로드한 사진. 파일은 S3에 두고 객체 키만 저장한다. 식사/메모 기록은 photo id로 참조한다.
 */
@Entity
@Table(name = "photo", indexes = @Index(name = "idx_photo_member", columnList = "member_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "storage_key", nullable = false, length = 200, unique = true)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 30)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    public Photo(Long memberId, String storageKey, String contentType, long sizeBytes) {
        this.memberId = memberId;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }
}
