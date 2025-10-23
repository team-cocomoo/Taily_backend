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

    private InquiryResponseDto reply;

    public static InquiryResponseDto from(Inquiry inquiry) {
        InquiryResponseDto.InquiryResponseDtoBuilder builder = InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .type(inquiry.getType())
                .state(inquiry.getState())
                .createdAt(inquiry.getCreatedAt())
                .userId(inquiry.getUser().getId())
                .nickname(inquiry.getUser().getNickname())
                .parentId(inquiry.getParentInquiry() != null ? inquiry.getParentInquiry().getId() : null);

        // 이미 fetch join 되어있으면 childInquiry도 안전하게 접근 가능
        if (inquiry.getChildInquiry() != null) {
            builder.reply(from(inquiry.getChildInquiry()));
        }

        return builder.build();
    }


}
