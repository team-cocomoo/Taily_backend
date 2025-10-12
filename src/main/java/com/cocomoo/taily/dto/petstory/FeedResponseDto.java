package com.cocomoo.taily.dto.petstory;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private String imageUrl;
    private List<String> tags;
    private Long likeCount;
    private Long view;
    private LocalDateTime createdAt;
}
