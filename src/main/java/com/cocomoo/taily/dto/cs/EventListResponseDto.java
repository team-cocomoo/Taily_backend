package com.cocomoo.taily.dto.cs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventListResponseDto {
    private Long id;          // 이벤트 ID (상세 페이지 이동용)
    private String imageUrl;  // 대표 이미지 경로
}