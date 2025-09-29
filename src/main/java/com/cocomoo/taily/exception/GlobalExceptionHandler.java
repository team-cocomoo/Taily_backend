package com.cocomoo.taily.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

/**
 * 전역 처리 예외 클래스
 *
 * @ControllerAdvice: 모든 Controller 에서 발생하는 Exception 을 한 곳에서 처리
 * 일관성 있는 에러 응답 형식 제공
 * Controller 들은 예외 처리 코드 작성이 필요 없음(관심사 분리->AOP)
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 예외 처리 메서드들

    /**
     * RuntimeException 처리
     * - 상품을 찾을 수 없습니다 등의 예외
     * - 404 Not Found 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalErrorResponse> handlerRuntimeException(RuntimeException e) {
        log.info("RuntimeException 발생하여 처리 {}", e.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(404)
                .code("NOT_FOUND")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * IllegalArgumentException
     * 재고 증가시 "수량은 양수이어야 합니다" 등의 입력값 오류
     * - 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handlerIllegalArgumentException(IllegalArgumentException e) {
        log.info("IllegalArgumentException 발생하여 처리 {}", e.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .code("BAD_REQUEST")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /*
        IllegalStateException
        재고가 부족합니다 등의 상태 오류
        - 409 Conflict 응답
     */

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GlobalErrorResponse> handlerIllegalStateException(IllegalStateException e) {
        log.info("IllegalStateException 발생하여 처리 {}", e.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(409)
                .code("CONFLICT")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 모든 예외의 최종 처리
     * 예상하지 못한 시스템 오류
     * - 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleException(Exception e){
        log.error("예상하지 못한 오류 발생 {}",e.getMessage());
        GlobalErrorResponse error = GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .code("INTERNAL_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

