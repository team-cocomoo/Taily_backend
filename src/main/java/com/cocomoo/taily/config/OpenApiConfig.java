package com.cocomoo.taily.config;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 설정 클래스
 * <p>
 * Swagger의 역할:
 * - REST API 문서 자동 생성
 * - API 테스트 UI 제공
 * - 프론트엔드 개발자와의 협업 도구
 * <p>
 * 접속 URL: http://localhost:8080/swagger-ui/index.html
 */
@Configuration // 현 클래스가 설정 클래스임을 스프링 컨테이너에 알림
@Tag(name = "Taily Management", description = "테일리 API") // OpenApi / Swagger Web UI 에 보여줄 내용
public class OpenApiConfig {
    @Bean // 현 메서드 반환 객체를 bean 으로 생성해 관리
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info().title("Taily Management API")
                        .description("펫 커뮤니티 관리를 위한 REST API")
                        .version("1.0.0")
                        .contact(new Contact().name("개발팀").email("dev@gmail.com")));
    }
}
