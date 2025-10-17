package com.cocomoo.taily.dto.inquiry;

import com.cocomoo.taily.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryCreateRequestDto {
    private String title;
    private String content;
    private Long parentId;
}
