//package com.cocomoo.taily.security.jwt;
//
//import com.cocomoo.taily.dto.ApiResponseDto;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
///**
// * 권한 부족시 처리하는 Handler
// * AccessDeniedHandler는 인증은 되었지만 해당 리소스에 대한 권한이 없을 때 실행됨
// */
//
//@Slf4j
//// 로그 사용을 위한 어노테이션
//@Component
//// 스프링 빈으로 등록되어서 Security 설정에서 자동 주입
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//    // AccessDeniedHandler : Spring Security의 권한 부족 처리 인터페이스 구현
//    private final ObjectMapper objectMapper = new ObjectMapper()
//            // ObjectMapper 자바 객체를 JSON으로 변환하기 위한 Jackson 라이브러리 객체
//            .registerModule(new JavaTimeModule())
//            // LocalDateTime 처리를 위한 모듈 등록
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//            // 날짜를 숫자 타임스탬프가 아니고 ISO-8601 형식으로 출력
//
//    /** 권한 부족시 자동으로 실행되는 메서드
//     * 사용자는 인증(Authentication)되었지만, 요청한 리소스의 권한(Authorization)이 부족할 때 발생
//     *
//     * @param request
//     * @param response
//     * @param accessDeniedException
//     * @throws IOException
//     * @throws ServletException
//     */
//    @Override
//    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
//
//        // 권한 부족 로그
//        // 어떤 URL 요청에서 권한 부족이 발생했는지 기록
//        log.error("권한 부족 sea : {} - {}",
//                request.getRequestURL(),
//                accessDeniedException.getMessage());
//        // ex) 권한 부족 sea : http://localhost:8080/admin - Access is denied
//
//        // ApiResponseDto 형식으로 응답 객체 생성
//        ApiResponseDto<?> errorResponse = ApiResponseDto.error(
//                "FORBIDDEN",
//                "해당 리소스에 접근할 권한이 없습니다."
//        );
//        /*
//        {
//            "status": "FORBIDDEN",
//            "message": "해당 리소스에 접근할 권한이 없습니다."
//        }
//         */
//
//        // HTTP 응답 설정
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 응답 상태 코드 : 403 Forbidden
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답 타입 application/json
//        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 인코딩 UTF-8로 설정
//
//        // ApiResponseDto 객체를 JSON으로 문자열로 변환
//        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
//    }
//}