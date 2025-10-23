package com.cocomoo.taily.dto.walkDiary;

import com.cocomoo.taily.entity.WalkDiaryEmotion;
import com.cocomoo.taily.entity.WalkDiaryWeather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * 산책 일지 수정 요청 DTO
 * - 클라이언트 → 서버로 전달되는 게시글 수정 정보
 * - 작성자는 Spring Security의 인증 정보에서 자동 추출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkDiaryUpdateRequestDto {
    private WalkDiaryWeather walkDiaryWeather;
    private LocalTime beginTime;
    private LocalTime endTime;
    private WalkDiaryEmotion walkDiaryEmotion;
    private String content;
    private List<Long> imageIds;
}
