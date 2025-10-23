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
    public ReportResponseDto createReport(ReportCreateRequestDto requestDto) {
        // publicId → User 조회
        User reporter = userRepository.findByPublicId(requestDto.getReporterPublicId())
                .orElseThrow(() -> new RuntimeException("신고자 없음"));
        User reported = userRepository.findById(requestDto.getReportedId())
                .orElseThrow(() -> new RuntimeException("신고 대상 없음"));

        // 중복 신고 확인
        boolean exists = reportRepository.existsByReporterAndReportedAndPath(
                reporter, reported, requestDto.getPath()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 신고한 게시글입니다.");
        }

        // 신고 저장
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .path(requestDto.getPath())
                .content(requestDto.getContent())
                .build();
        reportRepository.save(report);

        return ReportResponseDto.from(report);
    }

}
