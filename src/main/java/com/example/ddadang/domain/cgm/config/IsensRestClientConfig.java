package com.example.ddadang.domain.cgm.config;

import com.example.ddadang.domain.cgm.exception.IsensApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

@Configuration
@EnableConfigurationProperties(IsensProperties.class)
@RequiredArgsConstructor
public class IsensRestClientConfig {

    private final IsensProperties isensProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public RestClient isensAuthRestClient() {
        return RestClient.builder()
            .baseUrl(isensProperties.authBaseUrl())
            .defaultStatusHandler(status -> status.isError(), isensErrorHandler())
            .build();
    }

    @Bean
    public RestClient isensApiRestClient() {
        return RestClient.builder()
            .baseUrl(isensProperties.apiBaseUrl())
            .defaultStatusHandler(status -> status.isError(), isensErrorHandler())
            .build();
    }

    private ErrorHandler isensErrorHandler() {
        return (request, response) -> {
            throw new IsensApiException(response.getStatusCode(), extractErrorCode(response), readBody(response));
        };
    }

    private String readBody(ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * i-sens 에러 응답 바디 포맷이 명확히 문서화되어 있지 않아, "error" 필드가 있으면 그 값을,
     * 없으면 raw body를 errorCode 자리에 넣는다. (401 invalid_token / expired_token,
     * 400 required_input, 500 unexpected_error 문자열이 어딘가에는 포함되는 것으로 관찰됨)
     */
    private String extractErrorCode(ClientHttpResponse response) {
        String body = readBody(response);
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
            if (node.hasNonNull("error_code")) {
                return node.get("error_code").asText();
            }
        } catch (IOException ignored) {
            // body가 JSON이 아닌 경우 raw body를 그대로 사용
        }
        return body.isBlank() ? "unknown_error" : body;
    }
}
