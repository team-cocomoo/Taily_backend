package com.cocomoo.taily.dto.cs;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FaqPageResponseDto {
    private List<FaqDetailResponseDto> faqList;
    private long totalCount;
}
