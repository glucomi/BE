package com.example.ddadang.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Member 인증 도메인이 아직 없어 spring-security 기본 설정(전체 경로 basic auth 잠금)이
 * 켜진 상태였다. Swagger 문서/CGM API만 로컬 테스트용으로 열어두고 나머지는 인증을 요구한다.
 * TODO: Member 인증이 붙으면 /api/cgm/** 도 로그인 사용자 기준으로 좁힐 것.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/api/cgm/**",
        "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(PUBLIC_PATHS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
