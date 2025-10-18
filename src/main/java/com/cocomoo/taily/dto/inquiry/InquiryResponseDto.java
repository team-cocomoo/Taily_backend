package com.cocomoo.taily.dto.inquiry;

import com.cocomoo.taily.entity.Inquiry;
import com.cocomoo.taily.entity.InquiryState;
import com.cocomoo.taily.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDto {
    private Long id;
    private String title;
    private String content;
    private InquiryType type;
    private InquiryState state;
    private LocalDateTime createdAt;
    private Long userId;
    private String nickname;
    private Long parentId;

    public static InquiryResponseDto from(Inquiry inquiry) {
        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .type(inquiry.getType())
                .state(inquiry.getState())
                .createdAt(inquiry.getCreatedAt())
                .userId(inquiry.getUser().getId())
                .nickname(inquiry.getUser().getNickname())
                .parentId(inquiry.getParentInquiry() != null ? inquiry.getParentInquiry().getId() : null)
                .build();
    }
}
