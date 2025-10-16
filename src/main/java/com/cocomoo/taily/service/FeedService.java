package com.cocomoo.taily.service;

import com.cocomoo.taily.config.FileStorageProperties;
import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FeedRepository feedRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final TagRepository tagRepository;
    private final FileStorageProperties fileStorageProperties;

    // 업로드 경로 외부 설정 가능 (없으면 기본 상대경로)
    /**
     * Feed 등록 메서드
     * @param userId
     * @param dto
     * @return
     */
    @Transactional
    public FeedResponseDto registerFeed(Long userId, FeedRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TableType tableType = tableTypeRepository.findById(
                dto.getTableTypeId() != null ? dto.getTableTypeId() : 3L
        ).orElseThrow(() -> new RuntimeException("테이블 타입을 찾을 수 없습니다."));
        // 테이블 타입 없으면 예외 발생, 기본적으로 3L 사용

        // [] 사용자가 입력한 내용을 Feed 엔티티로 빌드
        Feed feed = Feed.builder()
                .content(dto.getContent())
                .user(user)
                .tableType(tableType)
                .build();
        feedRepository.save(feed);
        // JPA의 save()>를 호출해서 DB에 실제로 INSERT 한다.

        // [] Image 엔티티만 먼저 DB에 저장
        List<Image> images = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile file : dto.getImages()) {
                String uuid = UUID.randomUUID().toString();
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
                String newFileName = uuid + "_" + originalFileName;
                savedFileNames.add(newFileName);

                Image img = Image.builder()
                        .uuid(uuid)
                        .filePath("/uploads/feed/" + newFileName)
                        // 현재 프로젝트 루트 경로를 기준으로 저장, 배포시 변경(application.properties에 절대경로 저장
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(feed.getId())
                        .user(null)
                        // user 프로필이 아니기 때문에 null로 처리
                        .tableTypesId(3L)
                        // feed에서 사용하기 때문에 feed의 테이블 id인 3L 사용
                        .build();
                images.add(img);
            }
            imageRepository.saveAll(images);
        }

        // 태그 처리
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagName : dto.getTags()) {
                Tag tagEntity = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                TagList tagList = TagList.builder()
                        .feed(feed)
                        .tag(tagEntity)
                        .build();
                feed.getTagLists().add(tagList);
            }
        }

        // 트랜잭션 종료 후 파일 저장 (정합성 체크)
        try {

            saveFilesWithLog(savedFileNames, dto.getImages(),"feed");
        } catch (IOException e) {
            // 파일 저장 실패 시 DB에 저장된 이미지 삭제
            if (!images.isEmpty()) imageRepository.deleteAll(images);
            log.error("파일 저장 실패, DB 롤백 처리: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패, 트랜잭션 롤백", e);
        }

        return mapToDto(feed);
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


    // 피드와 피드에 연결된 모든 이미지를 리스트로 반환
    public FeedResponseDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드 없음"));

        feed.setView(feed.getView() + 1);
        feedRepository.save(feed);

        // 특정 피드에 해당되는 모든 이미지 반환
        List<Image> images = imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L);
        feed.getImages().clear();
        feed.getImages().addAll(images);

        return mapToDto(feed);
    }

    /**
     * 특정 게시글에 연결된 모든 이미지만 조회
     */
    public List<String> getImageByPost(Long postId, Long tableTypeId) {
        List<Image> images = imageRepository.findByPostsIdAndTableTypesId(postId, tableTypeId);

        //Image 엔티티에서 filePath만 추출하여 리스트로 반환
        return images.stream()
                .map(Image::getFilePath)
                .collect(Collectors.toList());
    }

    /**
     * 전체 피드 조회 메서드
     * @return
     */
    @Transactional(readOnly = true)
    public List<FeedResponseDto> getAllFeeds() {
        // 최신순으로 전체 Feed 조회
        List<Feed> feeds = feedRepository.findAllByOrderByCreatedAtDesc();

        // 각 피드에 이미지와 태그 매핑
        for (Feed feed : feeds) {
            // 이미지 조회
            List<Image> images = imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L);
            feed.getImages().clear();
            feed.getImages().addAll(images);
        }

        // DTO 변환
        return feeds.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드 없음"));

        if (!feed.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한 없음");
        }

        List<Image> images = imageRepository.findByPostsIdAndTableTypesId(feedId, 3L);
        imageRepository.deleteAll(images);

        feedRepository.delete(feed);
    }

    /**
     * 페이지 단위로 피드를 가져오는 메서드
     * @param page
     * @param size
     * @return
     */
    public Page<FeedResponseDto> getFeedsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Feed> feedPage = feedRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Feed -> DTO 변환
        List<FeedResponseDto> dtoList = feedPage.getContent().stream()
                .map(feed -> {
                    List<Image> images = imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L);
                    feed.getImages().clear();
                    feed.getImages().addAll(images);
                    return mapToDto(feed);
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, feedPage.getTotalElements());
    }

    @Transactional
    // [] feed와 연결된 모든 TagList 객체를 가져와서 리스트로 만든다.
    private FeedResponseDto mapToDto(Feed feed) {
        List<String> tagNames = feed.getTagLists().stream()
                .map(tagList -> tagList.getTag().getName())
                .toList();

        // 이미지 경로 리스트: 공통 테이블 구조를 고려하여 repository에서 직접 조회
        List<String> imagePaths = imageRepository
                .findByPostsIdAndTableTypesId(feed.getId(),3L) // 3L은 feed 테이블 타입
                .stream()
                .map(Image::getFilePath)
                .toList();

        return FeedResponseDto.builder()
                .id(feed.getId())
                .userId(feed.getUser().getId())
                .content(feed.getContent())
                .view(feed.getView())
                .likeCount(feed.getLikeCount())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .images(imagePaths)
                .tags(tagNames)
                .build();
    }



}