package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.walkDiary.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.repository.WalkDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WalkDiaryService {
    private final WalkDiaryRepository walkDairyRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final ImageRepository imageRepository;
    private final FileUploadService fileUploadService;

    @Value("${app.base-url}")
    private String baseUrl; // application.properties 값 주입

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
        log.info("=== 산책 일지 월별 조회 시작 ===");
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
     * @param walkDiaryCreateRequestDto 산책 일지 작성 정보
     * @return 생성된 산책 일지 상세 정보
     */
    @Transactional
    public WalkDiaryDetailResponseDto createWalkDiary(WalkDiaryCreateRequestDto walkDiaryCreateRequestDto, LocalDate date, String username) throws IOException {
        log.info("=== 산책 일지 작성 시작 : username={} ===", username);

        // 작성자 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. "));

        TableType tableType = tableTypeRepository.findById(4L).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // WalkDiary entity 생성
        WalkDiary walkDiary = WalkDiary.builder()
                .date(date)
                .walkDiaryWeather(WalkDiaryWeather.valueOf(walkDiaryCreateRequestDto.getWalkDiaryWeather()))
                .beginTime(LocalTime.parse(walkDiaryCreateRequestDto.getBeginTime()))
                .endTime(LocalTime.parse(walkDiaryCreateRequestDto.getEndTime()))
                .walkDiaryEmotion(WalkDiaryEmotion.valueOf(walkDiaryCreateRequestDto.getWalkDiaryEmotion()))
                .content(walkDiaryCreateRequestDto.getContent())
                .user(user)
                .build();

        // DB 저장
        WalkDiary savedWalkDiary = walkDairyRepository.save(walkDiary);

        // 이미지 저장
        List<ImageResponseDto> images = new ArrayList<>();
        if (walkDiaryCreateRequestDto.getImages() != null && !walkDiaryCreateRequestDto.getImages().isEmpty()) {
            Files.createDirectories(Paths.get("uploads/")); // 폴더 없을 경우 생성
            List<Image> imageEntities = new ArrayList<>();

            for (MultipartFile file : walkDiaryCreateRequestDto.getImages()) {
                String uuid = UUID.randomUUID().toString();
                String filename = uuid + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/", filename);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                String fileUrl = "/uploads/" + filename;

                Image image = Image.builder()
                        .uuid(uuid)
                        .filePath(fileUrl)
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(savedWalkDiary.getId())
                        .usersId(user)
                        .tableTypeId(tableType)
                        .build();
                imageEntities.add(image);

            }
                imageRepository.saveAll(imageEntities);
                images = imageEntities.stream().map(img -> ImageResponseDto.from(img, baseUrl)) // 여기서 baseUrl 전달
                        .toList();
        }

        log.info("산책 일지 작성 완료: id={}, title={}", savedWalkDiary.getId(), savedWalkDiary.getContent());

        return WalkDiaryDetailResponseDto.from(savedWalkDiary, images);
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
        log.info("산책 일지 상세 조회 : username = {}, date = {}", username, walkDiaryId);

        // 작성자 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        // 작성자 + 날짜로 산책 일지 찾기
        WalkDiary walkDiary = walkDairyRepository.findByIdAndUser(walkDiaryId, user.getUsername()).orElseThrow(() -> {
            log.warn("산책 일지 조회 실패 : username={}, walkDiaryId={}", username, walkDiaryId);
            return new IllegalArgumentException("존재하지 않는 산책 일지입니다.");
        });

        // 이미지 조회 + url 완성
        List<ImageResponseDto> images = imageRepository.findByPostsId(walkDiary.getId()).stream().map(img -> ImageResponseDto.from(img, baseUrl)).toList();

        log.info("산책 일지 조회 성공: content={}", walkDiary.getContent());

        return WalkDiaryDetailResponseDto.from(walkDiary, images);
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
    public WalkDiaryDetailResponseDto updateWalkDiary(Long walkDiaryId, WalkDiaryUpdateRequestDto walkDiaryUpdateRequestDto, String username, List<MultipartFile> newImages) throws IOException {
        // 기존 산책 일지 조회
        WalkDiary walkDiary = walkDairyRepository.findById(walkDiaryId).orElseThrow(() -> new IllegalArgumentException("산책 일지가 존재하지 않습니다."));

        // 작성자 검증
        if (!walkDiary.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인 산책 일지만 수정할 수 있습니다.");
        }

        // 기존 산책 일지 내용 업데이트
        walkDiary.updateWalkDiary(
                walkDiaryUpdateRequestDto.getWalkDiaryWeather(),
                walkDiaryUpdateRequestDto.getBeginTime(),
                walkDiaryUpdateRequestDto.getEndTime(),
                walkDiaryUpdateRequestDto.getWalkDiaryEmotion(),
                walkDiaryUpdateRequestDto.getContent()
        );

        // 이미지 처리
        List<ImageResponseDto> updatedImages = new ArrayList<>();

        if (newImages != null && !newImages.isEmpty()) {
            User user = walkDiary.getUser();
            TableType tableType = tableTypeRepository.findById(4L)
                    .orElseThrow(() -> new IllegalArgumentException("TableType가 존재하지 않습니다."));

            // 새 이미지 저장
            List<Image> savedImages = new ArrayList<>();
            for (MultipartFile file : newImages) {
                String filePath = fileUploadService.saveFile(file); // 파일 저장 후 URL 반환
                String uuid = UUID.randomUUID().toString();

                Image img = Image.builder()
                        .uuid(uuid)
                        .filePath(filePath)
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(walkDiary.getId())
                        .usersId(user)
                        .tableTypeId(tableType)
                        .build();

                savedImages.add(img);
            }

            imageRepository.saveAll(savedImages);

            updatedImages = savedImages.stream()
                    .map(img -> ImageResponseDto.from(img, baseUrl))
                    .toList();
        } else {
            // 기존 이미지 유지
            updatedImages = imageRepository.findByPostsId(walkDiary.getId())
                    .stream()
                    .map(img -> ImageResponseDto.from(img, baseUrl))
                    .toList();
        }

        return WalkDiaryDetailResponseDto.from(walkDiary, updatedImages);
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

    public WalkDiaryStatsResponseDto getMonthlyStats(String username) {
        // 작성자 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. "));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<WalkDiary> walkDiaries = walkDairyRepository.findAllByUserAndDateBetween(user, startOfMonth, endOfMonth);

        if (walkDiaries.isEmpty()) {
            return WalkDiaryStatsResponseDto.empty();
        }

        // 총 산책 횟수
        int totalWalks = walkDiaries.size();
        // 평균 시간 계산
        double avgMinutes = walkDiaries.stream().mapToLong(d -> Duration.between(d.getBeginTime(), d.getEndTime()).toMinutes()).average().orElse(0);
        // 연속 산책 일수 계산
        long streakDays = calculateStreak(walkDiaries);
        // 날짜별 산책 시간
        List<WalkDiaryStatsResponseDto.DailyStat> dailyStats = walkDiaries.stream().map(d -> new WalkDiaryStatsResponseDto.DailyStat(
                d.getDate(),
                Duration.between(d.getBeginTime(), d.getEndTime()).toMinutes()
        )).toList();
        // 알림 문구
        String reminderMessage = createReminderMessage(walkDiaries);

        return new WalkDiaryStatsResponseDto(
                totalWalks,
                avgMinutes,
                streakDays,
                dailyStats,
                reminderMessage
        );
    }

    // 통계 도와주는 메서드 - 추후 entity로 이동
    private long calculateStreak(List<WalkDiary> diaries) {
        List<LocalDate> sortedDates = diaries.stream()
                .map(WalkDiary::getDate)
                .sorted()
                .toList();

        long streak = 1, maxStreak = 1;
        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                streak++;
                maxStreak = Math.max(maxStreak, streak);
            } else streak = 1;
        }
        return maxStreak;
    }

    private String createReminderMessage(List<WalkDiary> diaries) {
        LocalDate lastDate = diaries.stream()
                .map(WalkDiary::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        if (lastDate.isBefore(LocalDate.now().minusDays(7))) {
            return "이번 주는 아직 산책을 안했어요 😢";
        }
        return "저번 주보다 산책 시간이 더 늘었어요! 👏";
    }

    public boolean existsByUserAndDate(User user, LocalDate date) {
        return walkDairyRepository.existsByUserAndDate(user,date);
    }
}
