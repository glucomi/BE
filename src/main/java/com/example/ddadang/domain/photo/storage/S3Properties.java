package com.example.ddadang.domain.photo.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record S3Properties(String bucket, String region, Duration presignedUrlValidity) {
}
