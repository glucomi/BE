package com.example.ddadang.domain.cgm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "isens")
public record IsensProperties(
    String authBaseUrl,
    String apiBaseUrl,
    String clientId,
    String clientSecret,
    String redirectUri,
    String scope
) {
}
