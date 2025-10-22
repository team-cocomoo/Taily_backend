package com.cocomoo.taily.service;

import com.cocomoo.taily.config.FileStorageProperties;
import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.walkDiary.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.repository.WalkDiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
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
    private final ImageService imageService;
    private final FileStorageProperties fileStorageProperties;

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
    public WalkDiaryDetailResponseDto createWalkDiary(WalkDiaryCreateRequestDto walkDiaryCreateRequestDto, LocalDate date, String username) {
        log.info("=== 산책 일지 작성 시작 : username={} ===", username);

        // 작성자 조회
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. "));

        TableType tableType = tableTypeRepository.findById(4L).orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        // 오늘 날짜 이후의 글을 작성할 경우 예외 처리
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("오늘 이후 날짜에는 산책 일지를 작성할 수 없습니다.");
        }

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

        // 업로드된 이미지 ID들을 일지에 연결
        if (walkDiaryCreateRequestDto.getImageIds() != null && !walkDiaryCreateRequestDto.getImageIds().isEmpty()) {
            List<Image> images = imageRepository.findAllById(walkDiaryCreateRequestDto.getImageIds());
            for (Image img : images) {
                img.setPostsId(savedWalkDiary.getId());
                img.setTableTypesId(4L); // WALK_DIARY
                img.setUser(user);
            }
            imageRepository.saveAll(images);
        }

        List<ImageResponseDto> imageDtos = imageRepository
                .findByPostsIdAndTableTypesId(savedWalkDiary.getId(), 4L)
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("산책 일지 작성 완료: id={}, content={}", savedWalkDiary.getId(), savedWalkDiary.getContent());


        return WalkDiaryDetailResponseDto.from(savedWalkDiary, imageDtos);
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
        List<ImageResponseDto> imageDtos = imageRepository
                .findByPostsIdAndTableTypesId(walkDiary.getId(), 4L)
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("산책 일지 조회 성공: content={}", walkDiary.getContent());

        return WalkDiaryDetailResponseDto.from(walkDiary, imageDtos);
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
    public WalkDiaryDetailResponseDto updateWalkDiary(
            Long walkDiaryId,
            WalkDiaryUpdateRequestDto walkDiaryUpdateRequestDto,
            String username
    ) {
        log.info("=== 산책 일지 수정 시작 : id={}, username={} ===", walkDiaryId, username);

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

        // TableType, User 가져오기
        User user = walkDiary.getUser();
        TableType tableType = tableTypeRepository.findById(4L)
                .orElseThrow(() -> new IllegalArgumentException("TableType이 존재하지 않습니다."));

        Long tableTypesId = 4L; // WALK_DIARY

        // ✅ 기존 이미지
        List<Image> existingImages = imageService.getImages(tableTypesId, null, walkDiary.getId());
        List<Long> existingIds = existingImages.stream().map(Image::getId).toList();

        // ✅ 요청으로 받은 유지/추가 이미지 목록
        List<Long> requestedIds = walkDiaryUpdateRequestDto.getImageIds() != null
                ? walkDiaryUpdateRequestDto.getImageIds()
                : List.of();

        // ✅ 삭제 대상
        List<Long> toDeleteIds = existingIds.stream()
                .filter(id -> !requestedIds.contains(id))
                .toList();

        if (!toDeleteIds.isEmpty()) {
            List<Image> toDelete = imageRepository.findAllById(toDeleteIds);
            for (Image img : toDelete) {
                try {
                    String relativePath = img.getFilePath().replaceFirst("^/+", "");
                    File file = new File(new File("").getAbsolutePath(), relativePath);
                    if (file.exists() && file.delete()) {
                        log.info("🗑️ 파일 삭제 완료: {}", file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.warn("파일 삭제 실패: {}", e.getMessage());
                }
            }
            imageRepository.deleteAll(toDelete);
        }

        // ✅ 추가 대상
        List<Long> toAddIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!toAddIds.isEmpty()) {
            List<Image> toAdd = imageRepository.findAllById(toAddIds);
            for (Image img : toAdd) {
                img.setPostsId(walkDiary.getId());
                img.setTableTypesId(tableTypesId);
                img.setUser(user); // ✅ user 연결 명시
            }
            imageRepository.saveAll(toAdd);
        }

        // ✅ 최종 이미지 목록
        List<Image> imageEntities = imageRepository.findByPostsIdAndTableTypesId(walkDiary.getId(), tableTypesId);
        List<ImageResponseDto> imageDtos = imageEntities.stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("✅ 산책 일지 수정 완료 (id={})", walkDiary.getId());
        return WalkDiaryDetailResponseDto.from(walkDiary, imageDtos);
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

    /**
     * 산책 일지 월별 통계
     * @param username
     * @return
     */
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
        // 시간별 산책 시간 (0~23시)
        Map<Integer, Long> hourlyMap = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyMap.put(i, 0L);
        }

        for (WalkDiary d : walkDiaries) {
            LocalTime begin = d.getBeginTime();
            LocalTime end = d.getEndTime();

            int startHour = begin.getHour();
            int endHour = end.getHour();

            for (int h = startHour; h <= endHour; h++) {
                long minutesInHour = Math.min(60, Duration.between(
                        LocalTime.of(h, 0),
                        h == endHour ? end : LocalTime.of(h + 1, 0)
                ).toMinutes());

                hourlyMap.put(h, hourlyMap.get(h) + minutesInHour);
            }
        }

        List<WalkDiaryStatsResponseDto.HourlyStat> hourlyStats = hourlyMap.entrySet().stream()
                .map(e -> new WalkDiaryStatsResponseDto.HourlyStat(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(WalkDiaryStatsResponseDto.HourlyStat::getHour))
                .toList();

        // 알림 문구
        String reminderMessage = createReminderMessage(walkDiaries);

        return WalkDiaryStatsResponseDto.builder()
                .totalWalks(totalWalks)
                .avgDurationMinutes(avgMinutes)
                .streakDays(streakDays)
                .dailyStat(dailyStats)
                .hourlyStat(hourlyStats)
                .reminderMessage(reminderMessage)
                .build();
    }

    // 통계 도와주는 메서드
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
        if (diaries == null || diaries.isEmpty()) {
            return "이번 달에는 아직 산책 기록이 없어요 🐾";
        }

        LocalDate now = LocalDate.now();
        // 이번 주 월요일 계산
        LocalDate startOfThisWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        // 저번 주 월요일 계산
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfThisWeek.minusDays(1);

        // 이번 주 총 산책 시간
        long thisWeekMinutes = diaries.stream()
                .filter(d -> !d.getDate().isBefore(startOfThisWeek)) // startOfThisWeek ~ now
                .mapToLong(d -> Duration.between(d.getBeginTime(), d.getEndTime()).toMinutes())
                .sum();

        // 저번 주 총 산책 시간
        long lastWeekMinutes = diaries.stream()
                .filter(d -> !d.getDate().isBefore(startOfLastWeek) && !d.getDate().isAfter(endOfLastWeek))
                .mapToLong(d -> Duration.between(d.getBeginTime(), d.getEndTime()).toMinutes())
                .sum();

        if (thisWeekMinutes == 0) {
            return "이번 주는 아직 산책을 안했어요 😢";
        } else if (thisWeekMinutes > lastWeekMinutes) {
            return "저번 주보다 산책 시간이 더 늘었어요! 👏";
        } else if (thisWeekMinutes < lastWeekMinutes) {
            return "이번 주 산책 시간이 조금 줄었네요 🐾 주말에 한 번 더 걸어볼까요?";
        } else {
            return "꾸준히 산책 중이에요! 👏";
        }
    }

    public boolean existsByUserAndDate(User user, LocalDate date) {
        return walkDairyRepository.existsByUserAndDate(user,date);
    }

    /**
     * 실제 파일 저장 메서드
     * @param savedFileNames
     * @param files
     * @throws IOException
     */
    @Transactional
    private void saveFilesWithLog(List<String> savedFileNames, List<MultipartFile> files, String subFolder) throws IOException {
        if (files == null || files.isEmpty()) return;

        // 프로젝트 루트 기준 절대 경로
        String projectRoot = new File("").getAbsolutePath();
        String uploadPath = projectRoot + File.separator + fileStorageProperties.getUploadDir();

        // 기능별 하위 폴더를 포함한 업로드 디렉토리
        File uploadDir = new File(uploadPath, subFolder);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("업로드 폴더 생성 실패: " + uploadDir.getAbsolutePath());
        }

        // 저장된 파일 추적용 리스트
        List<File> savedFiles = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileName = savedFileNames.get(i);
            File dest = new File(uploadDir, fileName);

            try {
                file.transferTo(dest); // 실제 저장
                savedFiles.add(dest);
                log.info("파일 저장 성공: {}", dest.getAbsolutePath());
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", dest.getAbsolutePath(), e);
                // 실패 시 지금까지 저장한 파일 삭제
                for (File f : savedFiles) {
                    if (f.exists()) f.delete();
                }
                throw e; // 예외 던져서 트랜잭션 롤백
            }
        }
    }
}
