package com.cocomoo.taily.dto.petstory;

import lombok.*;
import java.util.List;

/**
 * 피드 작성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedRequestDto {

    /** 피드 내용 (필수) */
    private String content;

    /** 업로드된 이미지의 URL (선택) */
    private String imageUrl;

    /** 태그 목록 (선택, 예: ["산책", "푸들"]) */
    private List<String> tags;

    /** 테이블 타입 ID (선택, 기본 3L) */
    private Long tableTypeId;

    // TODO: 추후 이미지 업로드 기능 추가 시 MultipartFile 사용
    // private MultipartFile imageFile;
}
