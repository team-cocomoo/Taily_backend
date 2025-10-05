package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.walkDiary.WalkDairyCreateRequestDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryDetailResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryListResponseDto;
import com.cocomoo.taily.dto.walkDiary.WalkDiaryUpdateRequestDto;
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

import java.time.LocalDate;
import java.time.YearMonth;
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

    /**
     * 현재 년도, 월 별로 작성한 산책 일지 조회
     *
     * GET http://localhost:8080/api/walk-diaries?year=2025&month=10
     *
     * {
     *     "username": "tailyUser"
     * }
     *
     * @param username
     * @param year
     * @param month
     * @return
     */
    public List<WalkDiaryListResponseDto> getWalkDiaryByMonth(String username, int year, int month) {
        log.info("=== 산책 일지 리스트 조회 시작 ===");
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<WalkDiary> walkDiaries = walkDairyRepository.findByMonth(username, start, end);

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

    /**
     * 특정 산책 일지 상세 조회
     *
     * GET http://localhost:8080/walk-diaries/1
     *
     * @param walkDiaryId
     * @param username
     * @return
     */
    @Transactional
    public WalkDiaryDetailResponseDto getWalkDiaryById(Long walkDiaryId, String username) {
        log.info("산책 일지 상세 조회 : id = {}", walkDiaryId);

        WalkDiary walkDiary = walkDairyRepository.findByIdWithUser(walkDiaryId).orElseThrow(() -> {
            log.info("산책 일지 상세 조회 실패: id={}", walkDiaryId);
            return new IllegalArgumentException("존재하지 않는 산책 일지 입니다.");

        });

        // 이미지 조회

        log.info("산책 일지 조회 성공: content={}", walkDiary.getContent());

        return WalkDiaryDetailResponseDto.from(walkDiary);
    }

    /**
     * 특정 산책 일지 수정
     * PUT http://localhost:8080/walk-diaries/2
     *
     * {
     *   "walkDiaryWeather": "SUNNY",
     *   "beginTime": "07:30",
     *   "endTime": "08:10",
     *   "walkDiaryEmotion": "SMILE",
     *   "content": "오늘은 날씨가 맑아서 산책하기 딱 좋았다!"
     * }
     *
     * @param walkDiaryId
     * @param walkDiaryUpdateRequestDto
     * @param username
     * @return
     */
    @Transactional
    public WalkDiaryDetailResponseDto updateWalkDiary(Long walkDiaryId, WalkDiaryUpdateRequestDto walkDiaryUpdateRequestDto, String username) {
        WalkDiary walkDiary = walkDairyRepository.findById(walkDiaryId).orElseThrow(() -> new IllegalArgumentException("산책 일지가 존재하지 않습니다."));

        if (!walkDiary.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 산책 일지만 수정할 수 있습니다.");
        }

        walkDiary.updateWalkDiary(
                walkDiaryUpdateRequestDto.getWalkDiaryWeather(),
                walkDiaryUpdateRequestDto.getBeginTime(),
                walkDiaryUpdateRequestDto.getEndTime(),
                walkDiaryUpdateRequestDto.getWalkDiaryEmotion(),
                walkDiaryUpdateRequestDto.getContent()
        );

        // 이미지 추가

        return WalkDiaryDetailResponseDto.from(walkDiary);
    }

    /**
     * 특정 산책 일지 삭제
     * DELETE http://localhost:8080/api/walk-diaries/2
     *
     * @param walkDiaryId
     * @param username
     */
    @Transactional
    public void deleteWalkDiary(Long walkDiaryId, String username) {
        WalkDiary walkDiary = walkDairyRepository.findById(walkDiaryId).orElseThrow(() -> new IllegalArgumentException("산책 일지가 존재하지 않습니다."));

        if (!walkDiary.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 산책 일지만 삭제할 수 있습니다.");
        }

        walkDairyRepository.delete(walkDiary);
    }
}
