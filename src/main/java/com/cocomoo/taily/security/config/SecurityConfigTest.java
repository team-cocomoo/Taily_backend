package com.cocomoo.taily.security.config;//package com.cocomoo.taily.security

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

///**
// * Spring Security 설정
// * JWT 기반 인증을 위한 보안 설정
// */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfigTest {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 람다 방식 권장
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

}

