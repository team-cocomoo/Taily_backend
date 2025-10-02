package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDairyCreateRequestDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryDetailResponseDto;
import com.cocomoo.taily.service.WalkDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/walk-dairies")
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryController {
    public final WalkDiaryService walkDiaryService;

//    @GetMapping
//    public ResponseEntity<?> getAllList() {
//        log.info("게시글 리스트 조회 요청 ");
//        List<WalkDairyResponseDto> walkDiaries = walkDiaryService.getAllList();
//
//        return null;
//    }

    @PostMapping
    public ResponseEntity<?> createWalkDiary (@RequestBody WalkDairyCreateRequestDto walkDairyCreateRequestDto) {
        log.info("산책 일지 작성 시작");
        WalkDiaryDetailResponseDto walkDiaryDetailResponseDto = walkDiaryService.createWalkDiary(walkDairyCreateRequestDto);
        log.info("산책 일지 작성 {}", walkDiaryDetailResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(walkDiaryDetailResponseDto, "산책 일지가 작성되었습니다."));
    }
}
