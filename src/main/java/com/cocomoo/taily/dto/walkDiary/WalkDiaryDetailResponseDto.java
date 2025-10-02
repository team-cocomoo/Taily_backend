package com.cocomoo.taily.dto.walkDiary;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkDiary;
import com.cocomoo.taily.entity.WalkDiaryWeather;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 산책 일지 상세 응답 DTO
 * - 산책 일지 상세 조회 시 모든 정보 포함
 * - 작성자 정보도 함께 제공
 * - 수정/삭제 권한 체크를 위한 username 포함
 */
@Getter
@Builder
public class WalkDiaryDetailResponseDto {
    private Long walkDairyId;
    private LocalDateTime date;
    private WalkDiaryWeather walkDiaryWeather;
    private String content;
    private LocalTime beginTime;
    private LocalTime endTime;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;

    /**
     * Entity -> Dto 변환 메서드
     * - 상세 정보 전체 포함
     * - 작성자 정보 함께 제공
     *
     * @param walkDiary 변환할 WalkDiary 엔티티
     * @return WalkDairyDetailResponseDto
     */
    public static WalkDiaryDetailResponseDto from(WalkDiary walkDiary) {
        User user = walkDiary.getUser();

        return WalkDiaryDetailResponseDto.builder()
                .walkDairyId(walkDiary.getId())
                .date(walkDiary.getDate())
                .walkDiaryWeather(walkDiary.getWalkDiaryWeather())
                .content(walkDiary.getContent())
                .beginTime(walkDiary.getBeginTime())
                .endTime(walkDiary.getEndTime())
                .userId(user.getId())
                .username(user.getUsername())
                .createdAt(walkDiary.getCreatedAt())
                .build();
    }
}
