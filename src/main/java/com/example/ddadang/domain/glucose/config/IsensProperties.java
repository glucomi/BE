package com.example.ddadang.domain.glucose.config;

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
