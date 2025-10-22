package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.mainPage.MainStatsResponseDto;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MainStatsService {

    private final MyPetRepository myPetRepository;
    private final FeedRepository feedRepository;
    private final WalkPathRepository walkPathRepository;
    private final UserRepository userRepository;

    /**
     * 메인페이지 통계 데이터 조회
     */
    public MainStatsResponseDto getMainStats() {
        Long petCount = myPetRepository.count();
        Long feedCount = feedRepository.count();
        Long walkCount = walkPathRepository.count();
        Long userCount = userRepository.countActiveUsers();

        return MainStatsResponseDto.builder()
                .petCount(petCount)
                .feedCount(feedCount)
                .walkCount(walkCount)
                .userCount(userCount)
                .build();
    }
}
