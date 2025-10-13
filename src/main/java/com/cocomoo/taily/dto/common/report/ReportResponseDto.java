package com.cocomoo.taily.dto.common.report;

import com.cocomoo.taily.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDto {
    private Long id;
    private String path;
    private String content;
    private Long reporterId;
    private Long reportedId;

    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .path(report.getPath())
                .content(report.getContent())
                .reporterId(report.getReporter_id().getId())
                .reportedId(report.getReported_id().getId())
                .build();
    }
}
