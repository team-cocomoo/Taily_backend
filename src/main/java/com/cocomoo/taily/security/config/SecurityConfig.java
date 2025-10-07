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
    // 인증 설정 객체
    private final AuthenticationConfiguration authenticationConfiguration;
    // JWT 토큰 생성 및 검증 유틸리티
    private final JwtUtil jwtUtil;
    // 예외 핸들러들
    /*
    Spring Security의 Filter는 DispatcherServlet 이전에 실행됩니다
    @ControllerAdvice는 Controller 레벨의 예외만 처리 가능합니다
    Filter 예외는 AuthenticationEntryPoint와 AccessDeniedHandler로 처리해야 합니다
    Client Request → [Filter Chain] → [DispatcherServlet] → [Controller]
                                          ↓
                                  인증 실패 시
                             AuthenticationEntryPoint (401)
                                          ↓
                                  권한 부족 시
                              AccessDeniedHandler (403)
     */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /*
     * Spring Security가 주입되면 내부적으로 글로벌 영역에  AuthenticationManager 는 자동으로 주입됨
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityConfig SecurityFilterChain 인증,인가 설정");
        // CSRF(Cross-Site Request Forgery) 보호를 비활성화합니다.
        // JWT와 같은 REST API에서는 보통 STATELESS 세션이므로 CSRF 공격으로부터 안전하여 비활성화합니다.
        http.csrf(auth -> auth.disable());

        // 폼 로그인 방식과 HTTP Basic 인증을 비활성화합니다.
        // JWT를 사용한 커스텀 로그인 방식을 사용하기 때문입니다.
        http.formLogin(auth -> auth.disable());
        http.httpBasic(auth -> auth.disable());

        ///////////////////////인증 인가에 대한 설정(개발자가 주로 확인)//////////////////////
        http.authorizeHttpRequests(auth -> auth
<<<<<<< HEAD
<<<<<<< HEAD
                //로그인 허용
                .requestMatchers("/api/users/login").permitAll()
                // POST 방식의 회원 가입은 인증없이 허용
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                // 관리자 모드는 인증과 ROLE_ADMIN 권한이 필요
                // ROLE_ 은 자동 삽입
                .requestMatchers("/admin").hasRole("ADMIN")
                // GET 방식, 전체 게시글 조회는 인증 없이 접근을 모두 허용
                .requestMatchers(HttpMethod.GET,"/api/posts").permitAll()
                // 참고 /api/products, /api/products/** 경로에 대한 접근을 모두 허용합니다
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated());
=======
                .requestMatchers("/api/auth/login").permitAll()
=======
                .requestMatchers("/api/users/login").permitAll()
>>>>>>> origin/feat/ohs-login&logout
                //로그인 허용
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                // POST 방식의 회원 가입은 인증없이 허용
                .requestMatchers("/admin").hasRole("ADMIN")
                // 관리자 모드는 인증과 ROLE_ADMIN 권한이 필요
                // ROLE_ 은 자동 삽입
                .requestMatchers("/admin/login").permitAll()
                // 관리자 로그인 페이지 인증 없이 접근 허용
                .requestMatchers(HttpMethod.GET,"/api/posts").permitAll()
                // GET 방식, 전체 게시글 조회는 인증 없이 접근을 모두 허용
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                // 참고 /api/products, /api/products/** 경로에 대한 접근을 모두 허용합니다
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                // swagger용 접근 경로 허용
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/api/mypage/**").permitAll()
                .anyRequest().authenticated());
                // 나머지 모든 요청은 인증 필요
>>>>>>> develop

        // 예외 처리 핸들러 등록
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 인증 실패
                .accessDeniedHandler(customAccessDeniedHandler)        // 권한 부족
        );
        // 세션 관리 정책을 STATELESS(무상태)로 설정합니다.
        // JWT를 사용하기 때문에 서버에 사용자 상태(세션)를 저장하지 않습니다.
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWTFilter를 LoginFilter 이전에 추가합니다.
        // 이 필터가 먼저 실행되어 요청 헤더의 JWT 토큰을 검증하고 인증 정보를 설정합니다.
        http.addFilterBefore(new JwtFilter(jwtUtil), JsonLoginFilter.class);

        // Spring Security의 UsernamePasswordAuthenticationFilter 자리에 커스텀 JsonLoginFilter 추가합니다.
        // 이 필터가 로그인 요청을 가로채서 로그인 검증 및 JWT 토큰을 생성하고 응답 헤더에 담아 보냅니다.
        http.addFilterAt(new JsonLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil),
                UsernamePasswordAuthenticationFilter.class);

        // 설정된 HttpSecurity 객체를 기반으로 SecurityFilterChain을 빌드하여 반환합니다.
        return http.build();
    }

    /**
     * *CORS(Cross-Origin Resource Sharing) 설정
     * *
     * * CORS란?
     * * - 다른 도메인에서 API를 호출할 때 필요한 보안 정책
     * * - React(localhost:3000) → Spring Boot(localhost:8080) 통신 허용
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // 리액트 앱의 출처
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        //이 부분을 추가하면 브라우저 콘솔창에 토큰 정보를 직접 확인할 있다
        config.addExposedHeader("Authorization");

        source.registerCorsConfiguration("/**", config);
        return source;

    }
}