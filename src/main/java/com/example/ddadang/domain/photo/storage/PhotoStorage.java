package com.example.ddadang.domain.photo.storage;

/**
 * 사진 파일 저장소. 운영은 S3, 테스트는 목으로 대체한다.
 */
public interface PhotoStorage {

    void upload(String key, byte[] content, String contentType);

    /** 만료 시간이 있는 조회용 URL */
    String getUrl(String key);
}
