package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.common.report.ReportCreateRequestDto;
import com.cocomoo.taily.dto.common.report.ReportResponseDto;
import com.cocomoo.taily.entity.Report;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.ReportRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 신고하기
    @Transactional
    public ReportResponseDto createReport(Long reporterId, ReportCreateRequestDto requestDto) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        User reported = userRepository.findById(requestDto.getReportedId())
                .orElseThrow(() -> new IllegalArgumentException("Reported user not found"));

        Report report = Report.builder()
                .reporter_id(reporter)
                .reported_id(reported)
                .path(requestDto.getPath())
                .content(requestDto.getContent())
                .build();

        reportRepository.save(report);

        return ReportResponseDto.from(report);
    }

    // 모든 신고 조회
    public List<ReportResponseDto> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }

    // 신고 ID당 조회
    public ReportResponseDto getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));
        return ReportResponseDto.from(report);
    }
}
