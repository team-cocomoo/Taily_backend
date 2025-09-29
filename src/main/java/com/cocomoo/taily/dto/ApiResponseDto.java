package com.cocomoo.taily.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
/**
  ApiResponse
 타입 안정성: 컴파일 시 필드와 타입을 체크할 수 있어 오류 방지
 일관성 유지: 공통 응답 구조를 강제할 수 있어 API가 통일됨
 문서화 편리: Swagger/OpenAPI에서 자동으로 스키마를 생성해줌
 */
@Getter
@Builder
public class ApiResponseDto<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final String code;
    private final LocalDateTime timestamp;

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDto<T> error(String code, String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
