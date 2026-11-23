package com.example.ddadang.domain.photo.storage;

import com.example.ddadang.domain.photo.status.PhotoErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * 비공개 S3 버킷에 사진을 올리고 presigned URL로 조회하게 한다.
 * 자격 증명은 SDK 기본 체인(EC2 인스턴스 프로파일, AWS_* 환경변수 등)을 따른다.
 */
@Slf4j
@Component
@EnableConfigurationProperties(S3Properties.class)
public class S3PhotoStorage implements PhotoStorage {

    private final S3Properties properties;
    private final S3Client s3Client;
    private final S3Presigner presigner;

    public S3PhotoStorage(S3Properties properties) {
        Region region = Region.of(properties.region());
        this.properties = properties;
        this.s3Client = S3Client.builder()
            .region(region)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();
        this.presigner = S3Presigner.builder().region(region).build();
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        try {
            s3Client.putObject(
                PutObjectRequest.builder().bucket(properties.bucket()).key(key).contentType(contentType).build(),
                RequestBody.fromBytes(content)
            );
        } catch (SdkException e) {
            log.error("S3 업로드 실패 key={}", key, e);
            throw new GeneralException(PhotoErrorStatus.UPLOAD_FAILED);
        }
    }

    @Override
    public String getUrl(String key) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
            .signatureDuration(properties.presignedUrlValidity())
            .getObjectRequest(GetObjectRequest.builder().bucket(properties.bucket()).key(key).build())
            .build();
        return presigner.presignGetObject(request).url().toString();
    }

    @PreDestroy
    void close() {
        s3Client.close();
        presigner.close();
    }
}
