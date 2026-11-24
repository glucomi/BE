package com.example.ddadang.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

class KakaoAuthorizeUrlIntegrationTest extends IntegrationTest {

    private static final String PATH = "/api/auth/kakao/authorize-url";
    private static final String WEB_REDIRECT = "https://ddadang.vercel.app/auth/kakao/callback";

    @Test
    void 프론트가_넘긴_state로_카카오_인가_URL을_만든다() throws Exception {
        String body = mockMvc.perform(get(PATH).param("redirectUri", WEB_REDIRECT).param("state", "abc-123_XYZ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.state").value("abc-123_XYZ"))
            .andReturn().getResponse().getContentAsString();

        String authorizeUrl = JsonPath.read(body, "$.result.authorizeUrl");
        UriComponents url = UriComponentsBuilder.fromUri(URI.create(authorizeUrl)).build(true);
        assertThat(url.getHost()).isEqualTo("localhost");
        assertThat(url.getPath()).isEqualTo("/oauth/authorize");
        MultiValueMap<String, String> query = url.getQueryParams();
        assertThat(query.getFirst("response_type")).isEqualTo("code");
        assertThat(query.getFirst("client_id")).isEqualTo("test-kakao-rest-api-key");
        assertThat(query.getFirst("state")).isEqualTo("abc-123_XYZ");
        // redirect_uri는 퍼센트 인코딩되어 들어간다
        assertThat(query.getFirst("redirect_uri")).isEqualTo("https%3A%2F%2Fddadang.vercel.app%2Fauth%2Fkakao%2Fcallback");
    }

    @Test
    void state를_안_넘기면_서버가_만들어_응답에_준다() throws Exception {
        String body = mockMvc.perform(get(PATH).param("redirectUri", "http://localhost:5000/auth/kakao/callback"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String state = JsonPath.read(body, "$.result.state");
        String authorizeUrl = JsonPath.read(body, "$.result.authorizeUrl");
        assertThat(state).matches("[A-Za-z0-9_-]{32}");
        assertThat(authorizeUrl).contains("state=" + state);
    }

    @Test
    void 인증_없이_호출할_수_있다() throws Exception {
        mockMvc.perform(get(PATH).param("redirectUri", WEB_REDIRECT))
            .andExpect(status().isOk());
    }

    @Test
    void 등록되지_않은_redirectUri면_400_AUTH_4000() throws Exception {
        mockMvc.perform(get(PATH).param("redirectUri", "https://evil.example.com/auth/kakao/callback"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AUTH_4000"));
        // 등록된 주소의 변형도 막는다
        mockMvc.perform(get(PATH).param("redirectUri", WEB_REDIRECT + "/"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AUTH_4000"));
    }

    @Test
    void state_형식이_잘못되면_400_AUTH_4001() throws Exception {
        mockMvc.perform(get(PATH).param("redirectUri", WEB_REDIRECT).param("state", "a&redirect_uri=x"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AUTH_4001"));
        mockMvc.perform(get(PATH).param("redirectUri", WEB_REDIRECT).param("state", "a".repeat(129)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AUTH_4001"));
    }

    @Test
    void redirectUri가_없으면_400() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isBadRequest());
    }
}
