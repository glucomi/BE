package com.example.ddadang.domain.member.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddadang.domain.member.status.AuthErrorStatus;
import com.example.ddadang.global.exception.GeneralException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 카카오 토큰 엔드포인트(/oauth/token)를 로컬 HTTP 서버로 대신해 요청 형식과 오류 매핑을 확인한다.
 */
class KakaoApiClientTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<String> responseBody = new AtomicReference<>("{}");
    private final AtomicReference<String> requestContentType = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> requestForm = new AtomicReference<>();
    private final AtomicInteger requestCount = new AtomicInteger();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/oauth/token", exchange -> {
            requestCount.incrementAndGet();
            requestContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requestForm.set(Arrays.stream(form.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                    pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                    pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8))));
            byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            exchange.sendResponseHeaders(responseStatus.get(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void 인가_코드를_form으로_보내_access_token을_받는다() {
        respond(200, """
            {"token_type":"bearer","access_token":"kakao-access","expires_in":21599,
             "refresh_token":"kakao-refresh","refresh_token_expires_in":5183999}
            """);

        KakaoTokenResponse token = client("rest-api-key", "")
            .exchangeCode("auth code/+=", "https://ddadang.vercel.app/auth/kakao/callback");

        assertThat(token.accessToken()).isEqualTo("kakao-access");
        assertThat(requestContentType.get()).startsWith("application/x-www-form-urlencoded");
        assertThat(requestForm.get()).containsOnly(
            Map.entry("grant_type", "authorization_code"),
            Map.entry("client_id", "rest-api-key"),
            Map.entry("redirect_uri", "https://ddadang.vercel.app/auth/kakao/callback"),
            Map.entry("code", "auth code/+=")
        );
    }

    @Test
    void client_secret이_설정돼_있으면_함께_보낸다() {
        respond(200, "{\"access_token\":\"kakao-access\"}");

        client("rest-api-key", "secret-value").exchangeCode("code", "http://localhost:3000/auth/kakao/callback");

        assertThat(requestForm.get()).containsEntry("client_secret", "secret-value");
    }

    @Test
    void 만료되거나_재사용된_코드는_INVALID_KAKAO_CODE() {
        respond(400, """
            {"error":"invalid_grant","error_description":"authorization code not found for code=x","error_code":"KOE320"}
            """);

        assertThatThrownBy(() -> client("rest-api-key", "").exchangeCode("x", "http://localhost:3000/cb"))
            .isInstanceOfSatisfying(GeneralException.class,
                e -> assertThat(e.getCode()).isEqualTo(AuthErrorStatus.INVALID_KAKAO_CODE));
    }

    @Test
    void REST_API_키나_Client_Secret_오류는_설정_오류로_구분한다() {
        respond(401, """
            {"error":"invalid_client","error_description":"Bad client credentials","error_code":"KOE010"}
            """);

        assertThatThrownBy(() -> client("wrong-key", "").exchangeCode("x", "http://localhost:3000/cb"))
            .isInstanceOfSatisfying(GeneralException.class,
                e -> assertThat(e.getCode()).isEqualTo(AuthErrorStatus.KAKAO_LOGIN_NOT_CONFIGURED));
    }

    @Test
    void 카카오_서버_오류는_KAKAO_SERVER_ERROR() {
        respond(503, "");

        assertThatThrownBy(() -> client("rest-api-key", "").exchangeCode("x", "http://localhost:3000/cb"))
            .isInstanceOfSatisfying(GeneralException.class,
                e -> assertThat(e.getCode()).isEqualTo(AuthErrorStatus.KAKAO_SERVER_ERROR));
    }

    @Test
    void REST_API_키가_없으면_카카오를_호출하지_않고_설정_오류() {
        assertThatThrownBy(() -> client("", "").exchangeCode("x", "http://localhost:3000/cb"))
            .isInstanceOfSatisfying(GeneralException.class,
                e -> assertThat(e.getCode()).isEqualTo(AuthErrorStatus.KAKAO_LOGIN_NOT_CONFIGURED));
        assertThat(requestCount.get()).isZero();
    }

    private KakaoApiClient client(String clientId, String clientSecret) {
        return new KakaoApiClient(baseUrl, baseUrl, clientId, clientSecret);
    }

    private void respond(int status, String body) {
        responseStatus.set(status);
        responseBody.set(body);
    }
}
