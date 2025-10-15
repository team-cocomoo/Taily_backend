package com.cocomoo.taily.dto.common.report;

import com.cocomoo.taily.entity.Report;
import com.cocomoo.taily.entity.ReportState;
import com.cocomoo.taily.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String reportedNickname;
    private String reporterNickname;
    private Long reportedUserId;
    private LocalDateTime createdAt;
    private ReportState state;

    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .path(report.getPath())
                .content(report.getContent())
                .reporterId(report.getReporter().getId())
                .reportedId(report.getReported().getId())
                .reportedNickname(report.getReported().getNickname())
                .reporterNickname(report.getReporter().getNickname())
                .reportedUserId(report.getReported().getId())
                .createdAt(report.getCreatedAt())
                .state(report.getState())
                .build();
    }
}
