package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDairyCreateRequestDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryDetailResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryListResponseDto;
import com.cocomoo.taily.service.WalkDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/walk-diaries")
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryController {
    public final WalkDiaryService walkDiaryService;

    @GetMapping
    public ResponseEntity<?> getAllWalkDiaries() {
        log.info("산책 일지 리스트 조회 요청 ");
        List<WalkDiaryListResponseDto> walkDiaries = walkDiaryService.getAllWalkDiaries();
        log.info("산책 일지 리스트 조회 완료 {} 건", walkDiaries.size());

        return ResponseEntity.ok(ApiResponseDto.success(walkDiaries, "산책 일지 리스트 조회 성공"));
    }

    @PostMapping
    public ResponseEntity<?> createWalkDiary (@RequestBody WalkDairyCreateRequestDto walkDairyCreateRequestDto) {
        log.info("산책 일지 작성 시작");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        log.info("산책 일지 작성, 작성자: username={}",username);

        WalkDiaryDetailResponseDto walkDiaryDetailResponseDto = walkDiaryService.createWalkDiary(walkDairyCreateRequestDto, username);
        log.info("산책 일지 작성 {}", walkDiaryDetailResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(walkDiaryDetailResponseDto, "산책 일지가 작성되었습니다."));
    }
}
