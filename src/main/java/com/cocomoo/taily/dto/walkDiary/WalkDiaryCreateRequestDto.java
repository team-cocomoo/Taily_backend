package com.cocomoo.taily.dto.walkDiary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 산책 일지 작성 요청 DTO
 * - 클라이언트 → 서버로 전달되는 게시글 작성 정보
 * - 작성자는 Spring Security의 인증 정보에서 자동 추출
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WalkDiaryCreateRequestDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String walkDiaryWeather;
    private String beginTime;
    private String endTime;
    private String walkDiaryEmotion;
    private String content;

    private List<MultipartFile> images;

    // userId는 별도로 받지 않음
    // - Spring Security에서 현재 로그인한 사용자 정보 사용
    // - SecurityContextHolder.getContext().getAuthentication()
    // - 보안상 더 안전한 방식

}
