package com.example.ddadang.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddadang.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class CorsIntegrationTest extends IntegrationTest {

    private static final String FRONT_ORIGIN = "https://ddadang.vercel.app";

    @Test
    void 허용된_origin의_preflight는_인증_없이_통과한다() throws Exception {
        mockMvc.perform(options("/api/weight-records/latest")
                .header(HttpHeaders.ORIGIN, FRONT_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization, content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT_ORIGIN))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "authorization, content-type"));
    }

    @Test
    void 허용된_origin의_실제_요청_응답에_CORS_헤더가_붙는다() throws Exception {
        mockMvc.perform(get("/api/weight-records/latest")
                .header(HttpHeaders.ORIGIN, FRONT_ORIGIN)
                .header(HttpHeaders.AUTHORIZATION, bearer(createMember())))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT_ORIGIN));
    }

    @Test
    void 허용되지_않은_origin의_preflight는_거부한다() throws Exception {
        mockMvc.perform(options("/api/weight-records/latest")
                .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
