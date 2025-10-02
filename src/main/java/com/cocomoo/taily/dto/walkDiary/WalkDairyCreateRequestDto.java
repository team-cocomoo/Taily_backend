package com.cocomoo.taily.dto.walkDiary;

import com.cocomoo.taily.entity.WalkDiaryWeather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 산책 일지 작성 요청 DTO
 * - 클라이언트 → 서버로 전달되는 게시글 작성 정보
 * - 작성자는 Spring Security의 인증 정보에서 자동 추출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkDairyCreateRequestDto {
    private LocalDateTime date;
    private WalkDiaryWeather walkDiaryWeather;
    private String content;
    private LocalTime beginTime;
    private LocalTime endTime;
    private Long userId;    // 테스트 용 (추후 제거)

    // userId는 별도로 받지 않음
    // - Spring Security에서 현재 로그인한 사용자 정보 사용
    // - SecurityContextHolder.getContext().getAuthentication()
    // - 보안상 더 안전한 방식

    /**
     * {
     *     "date": "2025-10-01",
     *     "walkDiaryWeather": "SUNNY",
     *     "content": "오늘은 산책하면서 꽃이 예뻤어요.",
     *     "beginTime": "09:00",
     *     "endTime": "10:00",
     *     "userId": 1
     * }
     */
}
