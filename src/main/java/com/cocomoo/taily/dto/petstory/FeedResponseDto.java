package com.cocomoo.taily.dto.petstory;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponseDto {
    private Long id; // 피드 pk
    private Long userId; // 작성자 pk
    private String content; // 콘텐츠 저장 내용
    private Long view; // 조회수
    private Long likeCount; // 좋아요 수
    private LocalDateTime createdAt; // 작성 시간
    private LocalDateTime updatedAt; // 수정 시간
    private List<String> images;  // 이미지 경로 리스트
    private List<String> tags;    // 태그 이름 리스트
}
