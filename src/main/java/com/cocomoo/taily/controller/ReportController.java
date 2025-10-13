package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.common.report.ReportCreateRequestDto;
import com.cocomoo.taily.dto.common.report.ReportResponseDto;
import com.cocomoo.taily.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // 신고 생성
    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(
            @RequestBody ReportCreateRequestDto requestDto) {

        ReportResponseDto response = reportService.createReport(requestDto);
        return ResponseEntity.ok(response);
    }


    // 모든 신고 조회
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getAllReports() {
        List<ReportResponseDto> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    // 특정 신고 조회
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long id) {
        ReportResponseDto report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }
}
