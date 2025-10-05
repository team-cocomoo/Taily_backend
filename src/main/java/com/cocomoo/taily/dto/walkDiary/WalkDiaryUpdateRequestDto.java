package com.cocomoo.taily.dto.walkDiary;

import com.cocomoo.taily.entity.WalkDiaryEmotion;
import com.cocomoo.taily.entity.WalkDiaryWeather;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

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
    @JsonFormat(pattern = "HH:mm")
    private LocalTime beginTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private WalkDiaryEmotion walkDiaryEmotion;
    private String content;
}
