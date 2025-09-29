package com.cocomoo.taily.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *  CORS 설정
 *  CORS: Cross Origin Resource Sharing 설정
 *
 *  Cors 란 ? 다른 도메인에서 API 를 호출할 때 필요한 보안 정책
 *            React (localhost: 5173) -> SpringBoot(localhost:8080) 통신 허용
 *            즉 Spring Boot API Server에 다른 어플리케이션의 React 앱이 접속할 수 있도록 허용
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // api 로 시작하는 모든 경로
                .allowedOrigins("http://localhost:5173") // 접속할 React 개발 서버 주소
                .allowedMethods("GET","POST","PUT","DELETE","PATCH") // 허용할 http method
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키 전송 허용
    }
}
