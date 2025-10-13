package com.cocomoo.taily.dto.petstory;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedRequestDto {

    private String content;                  // 피드 내용
    private Long tableTypeId;                // 테이블 타입 (optional)
    private List<MultipartFile> images;      // 실제 이미지 파일 업로드 (optional)
    private List<String> tags;               // 태그 목록 (optional)
}
