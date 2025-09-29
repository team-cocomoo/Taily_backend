package com.cocomoo.taily.exception;

import lombok.extern.slf4j.Slf4j;
import org.kosa.myproject.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 *   전역 처리 예외 클래스
 *   @ControllerAdvice : 모든 Controller 에서 발생하는 Exception 을 한 곳에서 처리
 *                                  일관성 있는 에러 응답 형식 제공
 *                                  Controller 들은 예외 처리 코드 작성이 필요 없음 ( 관심사 분리 -> AOP )
 *
 *    GlobalExceptionHandler 업데이트 내용
 *    1. 응답 형식 통일 (Controller와 동일한 구조) => ApiResponseDto 를 이용
 *   2. Spring Security 예외 처리 추가
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
/**
        * IllegalArgumentException 처리
     * 주로 유효성 검증 실패 시 발생
     * 예: 중복된 username, 존재하지 않는 회원 등
     */
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponseDto<?>> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("유효성 검증 실패: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error("BAD_REQUEST", e.getMessage()));
}

    /**
     * IllegalStateException 처리
     * 잘못된 상태에서 작업 시도 시 발생
     * 예: 인증되지 않은 사용자가 인증 필요 작업 시도
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<?>> handleIllegalStateException(IllegalStateException e) {
        log.warn("잘못된 상태: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error("CONFLICT", e.getMessage()));
    }

    /**
     * Spring Security 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAuthenticationException(AuthenticationException e) {
        log.error("인증 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.error("UNAUTHORIZED", "인증에 실패했습니다."));
    }


    /**
     * Spring Security 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("권한 부족: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.error("FORBIDDEN", "해당 작업을 수행할 권한이 없습니다."));
    }
    /**
     * RuntimeException 처리
     * 예상하지 못한 런타임 오류
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<?>> handleRuntimeException(RuntimeException e) {
        log.error("런타임 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("RUNTIME_ERROR", e.getMessage()));
    }

    /**
     * 모든 예외의 최종 처리
     * 위에서 처리되지 않은 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(Exception e) {
        log.error("예상하지 못한 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}




