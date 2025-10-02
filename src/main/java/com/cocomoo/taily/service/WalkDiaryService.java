package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.walkDiary.WalkDairyCreateRequestDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryDetailResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkDiary;
import com.cocomoo.taily.repository.DummyRepository;
import com.cocomoo.taily.repository.WalkDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryService {
    private final WalkDiaryRepository walkDairyRepository;
    private final DummyRepository dummyRepository;

    @Transactional
    public WalkDiaryDetailResponseDto createWalkDiary(WalkDairyCreateRequestDto walkDairyCreateRequestDto) {
        log.info("=== 산책 일지 작성 시작 ===");
        // 작성자 조회
        User user = dummyRepository.findById(walkDairyCreateRequestDto.getUserId()).orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없어 산책 일지를 등록할 수 없습니다. USER ID : " + walkDairyCreateRequestDto.getUserId()));

        // WalkDiary 생성
        WalkDiary walkDiary = WalkDiary.builder()
                .date(walkDairyCreateRequestDto.getDate())
                .walkDiaryWeather(walkDairyCreateRequestDto.getWalkDiaryWeather())
                .content(walkDairyCreateRequestDto.getContent())
                .beginTime(walkDairyCreateRequestDto.getBeginTime())
                .endTime(walkDairyCreateRequestDto.getEndTime())
                .user(user)
                .build();

        // DB 저장
        WalkDiary savedWalkDiary = walkDairyRepository.save(walkDiary);
        return WalkDiaryDetailResponseDto.from(savedWalkDiary);
    }
}
