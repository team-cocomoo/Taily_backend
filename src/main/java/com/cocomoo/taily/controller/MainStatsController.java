package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.mainPage.MainStatsResponseDto;
import com.cocomoo.taily.service.MainStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Slf4j
public class MainStatsController {

    private final MainStatsService mainStatsService;

    /**
     * 메인 페이지 통계 조회 API
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDto<MainStatsResponseDto>> getMainStats() {
        log.info("메인 통계 조회 요청 수신");

        MainStatsResponseDto stats = mainStatsService.getMainStats();

        return ResponseEntity.ok(
                ApiResponseDto.success(stats, "메인 페이지 통계 조회 성공")
        );
    }
}
