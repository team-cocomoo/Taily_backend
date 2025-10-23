package com.cocomoo.taily.dto.petstory;

import lombok.*;
import java.util.List;

/**
 * 🧩 FeedRequestDto
 * - 이미지 업로드는 /api/images/upload에서 따로 처리
 * - 여기서는 이미지 경로(filePath)만 받음
 * - 태그는 #을 포함하거나 제외해도 자동 처리됨
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedRequestDto {

    private String content;            // 피드 내용
    private Long tableTypeId;          // 테이블 타입 (기본값: 3L → FEED)
    private List<String> imagePaths;   // 업로드된 이미지 경로 리스트
    private List<String> tags;         // 태그 목록 (#포함 가능)
}
