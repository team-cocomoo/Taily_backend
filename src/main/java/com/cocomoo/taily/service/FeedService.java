package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final TagRepository tagRepository;

    @Transactional
    public FeedResponseDto createFeed(Long userId, FeedRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TableType tableType = tableTypeRepository.findById(
                dto.getTableTypeId() != null ? dto.getTableTypeId() : 3L
        ).orElseThrow(() -> new RuntimeException("테이블 타입을 찾을 수 없습니다."));

        Feed feed = Feed.builder()
                .content(dto.getContent())
                .user(user)
                .tableType(tableType)
                .build();

        feedRepository.save(feed);

        // 이미지 처리
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile file : dto.getImages()) {
                Image img = Image.builder()
                        .uuid(java.util.UUID.randomUUID().toString())
                        .filePath("/uploads/" + file.getOriginalFilename())
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(feed.getId())
                        .usersId(0L)
                        .tableTypesId(3L)
                        .build();
                imageRepository.save(img);
            }
        }

        // 태그 처리
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagName : dto.getTags()) {
                // Tag 엔티티 조회 또는 생성
                Tag tagEntity = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

                TagList tagList = TagList.builder()
                        .feed(feed)
                        .tag(tagEntity)
                        .build();

                feed.getTagLists().add(tagList);
            }
        }

        return mapToDto(feed);
    }

    @Transactional(readOnly = true)
    public FeedResponseDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드 없음"));

        feed.setView(feed.getView() + 1);
        feedRepository.save(feed);

        List<Image> images = imageRepository.findAllByPostsIdAndTableTypesId(feed.getId(), 3L);
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

        List<Image> images = imageRepository.findAllByPostsIdAndTableTypesId(feedId, 3L);
        imageRepository.deleteAll(images);

        feedRepository.delete(feed);
    }

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
