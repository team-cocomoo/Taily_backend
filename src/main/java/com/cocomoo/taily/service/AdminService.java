package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.admin.UserListResponseDto;
import com.cocomoo.taily.dto.admin.UserPageResponseDto;
import com.cocomoo.taily.dto.admin.UserPenaltyResponseDto;
import com.cocomoo.taily.dto.common.report.ReportResponseDto;
import com.cocomoo.taily.entity.Report;
import com.cocomoo.taily.entity.ReportState;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.repository.ReportRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * 전체 회원 리스트 + 검색 + 페이지네이션
     * GET http://localhost:8080/api/admin?keyword=taily&page=1&size=5
     *
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    public UserPageResponseDto getUsersPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAndSearchUser(keyword, pageable);

        List<UserListResponseDto> userList = usersPage.stream()
                .map(UserListResponseDto::from)
                .toList();
        return UserPageResponseDto.builder()
                .data(userList)
                .totalCount(usersPage.getTotalElements())
                .build();
    }

    // 모든 신고 조회
    public Page<ReportResponseDto> getReportsPage(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage = reportRepository.findByKeyword(keyword, pageable);

        return reportsPage.map(ReportResponseDto::from);
    }

    // 신고 ID당 조회
    public ReportResponseDto getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + reportId));
        return ReportResponseDto.from(report);
    }

    // 유저 제재
    @Transactional
    public UserPenaltyResponseDto suspendUser(Long userId, int days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        user.applyPenalty(days);
        log.info("{}일 제재 적용: {}, 종료일 {}", days, user.getUsername(), user.getPenaltyEndDate());

        return UserPenaltyResponseDto.from(user);
    }
    // 제재 해제 (스케줄러용)
    @Transactional
    public void restoreSuspendedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> expired = userRepository.findByStateAndPenaltyEndDateBefore(UserState.SUSPENDED, now);

        for (User user : expired) {
            user.liftPenalty();
        }

        if (!expired.isEmpty()) {
            log.info("{}명의 유저가 자동으로 ACTIVE 상태로 복구되었습니다.", expired.size());
        }
    }

    // 신고에서 유저 제재
    @Transactional
    public UserPenaltyResponseDto suspendUserByReport(Long reportId, int days) {
        // 신고 정보 가져오기
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        User user = userRepository.findById(report.getReported().getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 제재 적용 (days <= 0이면 내부에서 생략됨)
        if (days > 0) {
            if (user.getState() == UserState.SUSPENDED) {
                log.info("이미 제재 중인 유저: {}", user.getUsername());
            } else {
                user.applyPenalty(days);
                log.info("{}일 제재 적용: {}, 종료일 {}", days, user.getUsername(), user.getPenaltyEndDate());
            }
        } else {
            log.info("제재 없이 신고만 처리: reportId={}", report.getId());
        }

        // 신고 상태를 RESOLVED 로 변경 + updatedAt 갱신
        report.updateState(ReportState.RESOLVED);
        log.info("신고 처리 완료: reportId={}, state={}, updatedAt={}",
                report.getId(), report.getState(), report.getUpdatedAt());

        return UserPenaltyResponseDto.from(user);
    }




}
