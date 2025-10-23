package com.cocomoo.taily.dto.common.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequestDto {
    private String reporterPublicId;
    private Long reportedId;
    private String path;
    private String content;
}
