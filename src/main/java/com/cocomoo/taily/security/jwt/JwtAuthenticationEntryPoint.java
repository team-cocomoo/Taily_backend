package com.cocomoo.taily.security.jwt;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
    JWT 인증 실패 시 처리하는 EntryPoint
 AuthenticationEntryPoint는 인증되지 않은 사용자가 시큐리티로 보호된
 자원에 접근할 때 실행됩니다

 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // LocalDateTime 처리를 위한 모듈 등록
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO-8601 형식으로 출력

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        // 로그를 통해 어떤 인증 실패인지 확인
        log.error("인증 실패: {}", authException.getMessage());

        // JWT 관련 예외 메시지를 구체적으로 처리
        String errorMessage = getErrorMessage(request, authException);

        // ApiResponseDto 형식으로 응답 생성
        ApiResponseDto<?> errorResponse = ApiResponseDto.error(
            "UNAUTHORIZED",
            errorMessage
        );

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // JSON으로 변환하여 응답 작성
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * request attribute에서 JWT 검증 실패 이유를 확인하여
     * 더 구체적인 에러 메시지 제공
     */
    private String getErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        // JWTFilter에서 설정한 예외 정보 확인
        final String expired = (String) request.getAttribute("expired");
        final String invalid = (String) request.getAttribute("invalid");

        if (expired != null) {
            return "JWT 토큰이 만료되었습니다. 다시 로그인해주세요.";
        } else if (invalid != null) {
            return "유효하지 않은 JWT 토큰입니다.";
        }

        // 기본 메시지
        return "인증이 필요합니다. 로그인해주세요.";
    }
}