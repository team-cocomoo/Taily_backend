package com.cocomoo.taily.security.config;

import com.cocomoo.taily.security.jwt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정
 * JWT 기반 인증을 위한 보안 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityConfig SecurityFilterChain 인증, 인가 설정");

        // CSRF 비활성화 (REST API + JWT 특성상 불필요)
        http.csrf(csrf -> csrf.disable());

        // 기본 로그인/HTTP Basic 비활성화
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // 인가 규칙
        http.authorizeHttpRequests(auth -> auth
                // 비회원 접근 허용
                .requestMatchers(
                        "/api/users/login",
                        "/api/users/register",
                        "/api/auth/**",
                        "/api/facilities/**",         // 동물 관련 시설 조회
                        "/api/events/public/**",      // 이벤트 조회용
                        "/api/notices/public/**",     // 공지사항 목록
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api-docs/**"
                ).permitAll()

                // 회원 전용 (ROLE_USER)
                .requestMatchers(
                        "/api/mypage/**",
                        "/api/walk-diaries/**",
                        "/api/taily-friends/**",
                        "/api/feeds/**",
                        "/api/follows/**",
                        "/api/chats/**",
                        "/api/events/**",
                        "/api/notices/**",
                        "/api/qna/**"
                ).hasRole("USER")

                // 관리자 전용 (ROLE_ADMIN)
                .requestMatchers(
                        "/api/admin/**",
                        "/api/manage/**"
                ).hasRole("ADMIN")

                // 그 외 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // 인증/인가 실패 처리
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
                .accessDeniedHandler(customAccessDeniedHandler)         // 403
        );

        // 세션 사용 안 함 (JWT 기반 → STATELESS)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 필터 추가
        http.addFilterBefore(new JwtFilter(jwtUtil), JsonLoginFilter.class);

        // 커스텀 로그인 필터 등록
        http.addFilterAt(
                new JsonLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // 리액트 앱 주소
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization"); // 토큰 헤더 노출

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
