package com.example.ddadang.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * allowedOrigins는 정확히 일치하는 origin, allowedOriginPatterns는 포트 등에 와일드카드(*)를 쓰는 패턴.
 */
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
    List<String> allowedOrigins,
    List<String> allowedOriginPatterns
) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins;
        allowedOriginPatterns = allowedOriginPatterns == null ? List.of() : allowedOriginPatterns;
    }
}
