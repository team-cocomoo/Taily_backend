package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.alarm.AlarmResponseDto;
import com.cocomoo.taily.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {
    private final AlarmService alarmService;

    @PostMapping
    public ResponseEntity<?> getAlarms() {
        log.info("내 알람 리스트 조회");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<AlarmResponseDto> alarmList = alarmService.getAlarms(username);

        return ResponseEntity.ok(ApiResponseDto.success(alarmList, "알람 리스트 조회 성공"));
    }

    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long alarmId) {
        alarmService.markAsRead(alarmId);
        return ResponseEntity.ok(ApiResponseDto.success(null, "알람 읽음 처리완료"));
    }
}
