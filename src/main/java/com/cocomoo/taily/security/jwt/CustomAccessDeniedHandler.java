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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 권한 부족시 처리하는 Handler
 AccessDeniedHandler는 인증은 되었지만 해당 리소스에 대한 권한이 없을 때 실행됨
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // LocalDateTime 처리를 위한 모듈 등록
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO-8601 형식으로 출력

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 권한 부족 로그
        log.error("권한 부족 sea: {} - {}",
                request.getRequestURI(),
                accessDeniedException.getMessage());

        // ApiResponseDto 형식으로 응답 생성
        ApiResponseDto<?> errorResponse = ApiResponseDto.error(
                "FORBIDDEN",
                "해당 리소스에 접근할 권한이 없습니다."
        );

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // JSON으로 변환하여 응답 작성
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}