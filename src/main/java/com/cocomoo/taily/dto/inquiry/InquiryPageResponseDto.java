package com.cocomoo.taily.dto.inquiry;

import com.cocomoo.taily.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryPageResponseDto {
    private List<InquiryResponseDto> inquiries;
    private int currentPage;
    private int pageSize;
    private long totalCount;
    private int totalPages;

    public static InquiryPageResponseDto from(Page<Inquiry> inquiryPage, int currentPage, int pageSize) {
        List<InquiryResponseDto> content = inquiryPage.getContent().stream()
                .map(InquiryResponseDto::from)
                .collect(Collectors.toList());

        return InquiryPageResponseDto.builder()
                .inquiries(content)
                .currentPage(currentPage + 1)  // 클라이언트 기준 1부터 시작
                .pageSize(pageSize)
                .totalCount(inquiryPage.getTotalElements())
                .totalPages(inquiryPage.getTotalPages())
                .build();
    }
}
