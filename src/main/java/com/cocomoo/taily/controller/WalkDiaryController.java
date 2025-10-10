package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.walkDiary.*;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.WalkDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/walk-diaries")
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryController {
    public final WalkDiaryService walkDiaryService;

    // 년도, 월 별 산책 일지 조회
    @GetMapping
    public ResponseEntity<?> getWalkDiaryByMonth(@RequestParam int year, @RequestParam int month) {
        log.info("산책 일지 리스트 조회 요청 ");
        log.info("요청 year={}, month={}", year, month);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        List<WalkDiaryListResponseDto> walkDiaries = walkDiaryService.getWalkDiaryByMonth(username, year, month);
        log.info("산책 일지 리스트 조회 완료 {} 건", walkDiaries.size());

        return ResponseEntity.ok(ApiResponseDto.success(walkDiaries, "산책 일지 리스트 조회 성공"));
    }

    // 산책 일지 유무 체크
    @GetMapping("/check")
    public ResponseEntity<?> checkWalkDiary(@AuthenticationPrincipal CustomUserDetails user, @RequestParam("date") LocalDate date) {
        boolean exists = walkDiaryService.existsByUserAndDate(user.getUser(), date);

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 산책 일지 작성
    @PostMapping(value = "/write/{date}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWalkDiary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestPart("walkDiaryWeather") String walkDiaryWeather,
            @RequestPart("beginTime") String beginTime,
            @RequestPart("endTime") String endTime,
            @RequestPart("walkDiaryEmotion") String walkDiaryEmotion,
            @RequestPart("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {

        log.info("산책 일지 작성 시작");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("산책 일지 작성, 작성자: username={}", username);

        // DTO 생성
        WalkDiaryCreateRequestDto walkDiaryCreateRequestDto = WalkDiaryCreateRequestDto.builder()
                .date(date)
                .walkDiaryWeather(walkDiaryWeather)
                .beginTime(beginTime)
                .endTime(endTime)
                .walkDiaryEmotion(walkDiaryEmotion)
                .content(content)
                .images(images)
                .build();

        WalkDiaryDetailResponseDto walkDiaryDetailResponseDto =
                walkDiaryService.createWalkDiary(walkDiaryCreateRequestDto, date, username);

        log.info("산책 일지 작성 완료: {}", walkDiaryDetailResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(walkDiaryDetailResponseDto, "산책 일지가 작성되었습니다."));
    }

    // 산책 일지 상세 조회
    @GetMapping("/{date}")
    public ResponseEntity<?> getWalkDiaryByDate(@PathVariable LocalDate date) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        WalkDiaryDetailResponseDto walkDiary = walkDiaryService.getWalkDiaryById(date, username);

        log.info("산책 일지 상세 조회 성공: date={}", walkDiary.getDate());
        return ResponseEntity.ok(ApiResponseDto.success(walkDiary, "산책 일지 상세 조회 성공"));
    }

    // 추후 date로 변경
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWalkDiary(@PathVariable Long id, @RequestBody WalkDiaryUpdateRequestDto walkDiaryUpdateRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        log.info("산책 일지 수정, 작성자: username={}",username);

        WalkDiaryDetailResponseDto updatedWalkDiary = walkDiaryService.updateWalkDiary(id, walkDiaryUpdateRequestDto, username);

        return ResponseEntity.ok(ApiResponseDto.success(updatedWalkDiary, "산책 일지 수정 성공"));
    }

    // 추후 date로 변경
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWalkDiary(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        log.info("산책 일지 삭제, 작성자 {}", username);

        walkDiaryService.deleteWalkDiary(id, username);

        return ResponseEntity.ok(ApiResponseDto.success(null, "산책 일지 삭제 성공"));
    }

    // 산책 일지 월간 통계
    @GetMapping("/stats")
    public ResponseEntity<?> getMonthlyStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        WalkDiaryStatsResponseDto statsDto = walkDiaryService.getMonthlyStats(username);
        return ResponseEntity.ok(ApiResponseDto.success(statsDto, "월간 산책 통계 조회 성공"));
    }
}
