package com.cocomoo.taily.dto.notice;

import com.cocomoo.taily.dto.admin.AdminSummaryDto;
import com.cocomoo.taily.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long view;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AdminSummaryDto admin; // 작성자(관리자) 요약 정보

    public static NoticeResponseDto fromEntity(Notice notice) {
        return NoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .view(notice.getView())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .admin(AdminSummaryDto.fromEntity(notice.getUser()))
                .build();
    }
}
