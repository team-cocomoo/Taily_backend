package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.cs.EventDetailResponseDto;
import com.cocomoo.taily.dto.cs.EventListResponseDto;
import com.cocomoo.taily.dto.cs.EventPageResponseDto;
import com.cocomoo.taily.dto.cs.FaqPageResponseDto;
import com.cocomoo.taily.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evnets")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<?> getAppEvents(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size){
        log.info("faq 리스트 조회 시작");
        EventPageResponseDto result = eventService.getEventPage(page - 1, size);
        log.info("faq 리스트 조회 요청 {}", result.getTotalCount());

        return ResponseEntity.ok(ApiResponseDto.success(result, "전체 event 리스트 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        EventDetailResponseDto event = eventService.getEventDetail(id,username);

        return ResponseEntity.ok(ApiResponseDto.success(event,"event 상세 조회 성공"));
    }
}
