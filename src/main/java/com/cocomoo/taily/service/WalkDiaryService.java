package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.walkDiary.WalkDairyCreateRequestDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryDetailResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryListResponseDto;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkDiary;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.repository.WalkDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryService {
    private final WalkDiaryRepository walkDairyRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;

    public List<WalkDiaryListResponseDto> getAllWalkDiaries() {
        log.info("=== 산책 일지 리스트 조회 시작 ===");
        List<WalkDiary> walkDiaries = walkDairyRepository.findAllWithUser();

        log.info("조회된 산책 일지 수 : {}", walkDiaries.size());

        return walkDiaries.stream().map(WalkDiaryListResponseDto::from).collect(Collectors.toList());
    }

    /**
     * 산책 일지 작성
     *
     * {
     *   "date": "2025-10-01",
     *   "walkDiaryWeather": "SUNNY",
     *   "walkDiaryEmotion": "LOVE",
     *   "content": "오늘은 산책하면서 꽃이 예뻤어요.",
     *   "beginTime": "09:00",
     *   "endTime": "10:00",
     *   "userId": 1
     * }
     *
     * @param walkDairyCreateRequestDto 산책 일지 작성 정보
     * @return 생성된 산책 일지 상세 정보
     */
    @Transactional
    public WalkDiaryDetailResponseDto createWalkDiary(WalkDairyCreateRequestDto walkDairyCreateRequestDto, String username) {
        log.info("=== 산책 일지 작성 시작 : username={} ===", username);

        // 작성자 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. "));

        TableType tableType = tableTypeRepository.findById(4L).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // WalkDiary entity 생성
        WalkDiary walkDiary = WalkDiary.builder()
                .date(walkDairyCreateRequestDto.getDate())
                .walkDiaryWeather(walkDairyCreateRequestDto.getWalkDiaryWeather())
                .beginTime(walkDairyCreateRequestDto.getBeginTime())
                .endTime(walkDairyCreateRequestDto.getEndTime())
                .walkDiaryEmotion(walkDairyCreateRequestDto.getWalkDiaryEmotion())
                .content(walkDairyCreateRequestDto.getContent())
                .user(user)
                .build();

        // DB 저장
        WalkDiary savedWalkDiary = walkDairyRepository.save(walkDiary);

        // 이미지 추가

        log.info("산책 일지 작성 완료: id={}, title={}", savedWalkDiary.getId(), savedWalkDiary.getContent());

        return WalkDiaryDetailResponseDto.from(savedWalkDiary);
    }


}
