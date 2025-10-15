package com.cocomoo.taily.scheduler;

import com.cocomoo.taily.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPenaltyScheduler {

    private final AdminService adminService;

    // 매일 1시마다 제재 해제
    @Scheduled(cron = "0 0 1 * * *")
    public void restoreSuspendedUsers() {
        log.info("자동 제재 해제 스케줄러 실행");
        adminService.restoreSuspendedUsers();
    }
}
