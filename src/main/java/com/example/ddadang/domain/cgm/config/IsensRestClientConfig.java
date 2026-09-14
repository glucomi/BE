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

/**
 * Spring Boot 4.x는 기본 Jackson 빈을 Jackson 3(tools.jackson.databind.ObjectMapper)로 구성하므로
 * classic com.fasterxml.jackson.databind.ObjectMapper 빈은 컨텍스트에 없다. 에러 바디를
 * best-effort로 파싱하는 용도일 뿐이라 DI 없이 직접 생성해 사용한다.
 */
@Configuration
@EnableConfigurationProperties(IsensProperties.class)
@RequiredArgsConstructor
public class IsensRestClientConfig {

    private final IsensProperties isensProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            String body = readBody(response);
            throw new IsensApiException(response.getStatusCode(), extractErrorCode(body), extractMessage(body, body));
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
     * doc(이벤트 조회 API)에서 에러 응답 포맷이 {"code", "message", "param"}으로 확인됐다.
     * 다른 엔드포인트에서 다른 포맷("error"/"error_code")이 관찰될 수 있어 fallback으로 남겨둔다.
     */
    private String extractErrorCode(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("code")) {
                return node.get("code").asText();
            }
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

    private String extractMessage(String body, String fallback) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("message")) {
                String param = node.hasNonNull("param") ? " (param: " + node.get("param").asText() + ")" : "";
                return node.get("message").asText() + param;
            }
        } catch (IOException ignored) {
            // body가 JSON이 아닌 경우 raw body를 그대로 사용
        }
        return fallback;
    }
}
