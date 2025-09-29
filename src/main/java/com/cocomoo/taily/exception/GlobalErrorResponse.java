package com.cocomoo.taily.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Http Response Error 용 Dto: Nested Class(Inner Class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalErrorResponse {
    // 오류 발생 시간
    private LocalDateTime timestamp;
    // HTTP Response 상태 코드 (404, 405, 500 등)
    private int status;
    // 에러 코드(NOT_FOUND, BAD_REQUEST 등)
    private String code;
    // 사용자에게 보여줄 메세지
    private String message;
}