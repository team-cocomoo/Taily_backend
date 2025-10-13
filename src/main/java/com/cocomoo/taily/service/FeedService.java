package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final TagRepository tagRepository;

    // 업로드 경로 외부 설정 가능 (없으면 기본 상대경로)
    @Value("${app.upload-path:uploads/feed/}")
    private String uploadPath; // 기본 상대경로

    /**
     * Feed 등록 메서드
     * @param userId
     * @param dto
     * @return
     */
    @Transactional
    public FeedResponseDto createFeed(Long userId, FeedRequestDto dto) {
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
            saveFilesWithLog(savedFileNames, dto.getImages());
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
    private void saveFilesWithLog(List<String> savedFileNames, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("업로드 폴더 생성 실패: " + uploadPath);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileName = savedFileNames.get(i);
            File dest = new File(uploadDir, fileName);

            try {
                file.transferTo(dest);
                log.info("파일 저장 성공: {}", fileName);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", fileName, e);
                // [] 실패 시 이미 저장된 파일 삭제
                for (String fn : savedFileNames) {
                    File f = new File(uploadDir, fn);
                    if (f.exists()) f.delete();
                }
                throw e;
            }
        }
    }



    @Transactional(readOnly = true)
    public FeedResponseDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드 없음"));

        feed.setView(feed.getView() + 1);
        feedRepository.save(feed);

        List<Image> images = imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L);
        feed.getImages().clear();
        feed.getImages().addAll(images);

        return mapToDto(feed);
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

    // [] feed와 연결된 모든 TagList 객체를 가져와서 리스트로 만든다.
    private FeedResponseDto mapToDto(Feed feed) {
        List<String> tagNames = feed.getTagLists().stream()
                .map(tagList -> tagList.getTag().getName())
                .toList();

        return FeedResponseDto.builder()
                .id(feed.getId())
                .userId(feed.getUser().getId())
                .content(feed.getContent())
                .view(feed.getView())
                .likeCount(feed.getLikeCount())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .images(feed.getImages().stream().map(Image::getFilePath).toList())
                .tags(tagNames)
                .build();
    }
}