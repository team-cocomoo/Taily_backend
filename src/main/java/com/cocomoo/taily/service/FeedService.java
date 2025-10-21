package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
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
    private final TagListRepository tagListRepository;
    private final CommentRepository commentRepository;
    private final AlarmService alarmService;

    /**
     * Feed 등록 (이미지 경로 기반)
     */
    @Transactional
    public FeedResponseDto registerFeed(Long userId, FeedRequestDto dto) {
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

        // 이미지 저장
        if (dto.getImagePaths() != null && !dto.getImagePaths().isEmpty()) {
            List<Image> images = dto.getImagePaths().stream()
                    .map(path -> Image.builder()
                            .uuid(UUID.randomUUID().toString())
                            .filePath(path)
                            .fileSize("1") // null 방지 기본값
                            .tableTypesId(3L)
                            .postsId(feed.getId())
                            .user(user)
                            .build())
                    .collect(Collectors.toList());
            imageRepository.saveAll(images);
        }

        // 태그 등록
        saveOrUpdateTags(feed, dto.getTags());

        log.info("피드 등록 완료: feedId={}, userPublicId={}, images={}",
                feed.getId(), user.getPublicId(), dto.getImagePaths());
        return mapToDto(feed);
    }


    /**
     * Feed 수정 (내용, 태그, 이미지 갱신)
     */
    @Transactional
    public FeedResponseDto updateFeed(Long feedId, Long userId, FeedRequestDto dto) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드를 찾을 수 없습니다."));

        if (!feed.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        feed.setContent(dto.getContent());

        // 기존 이미지 삭제 후 새 이미지 등록
        imageRepository.deleteAll(imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L));

        if (dto.getImagePaths() != null && !dto.getImagePaths().isEmpty()) {
            List<Image> images = dto.getImagePaths().stream()
                    .filter(Objects::nonNull)
                    .map(path -> Image.builder()
                            .uuid(UUID.randomUUID().toString())
                            .filePath(path)
                            .fileSize("1") // null 방지
                            .tableTypesId(3L)
                            .postsId(feed.getId())
                            .user(feed.getUser()) // 유저 연관 설정
                            .build())
                    .collect(Collectors.toList());
            imageRepository.saveAll(images);
        }

        // 태그 갱신
        tagListRepository.deleteAll(tagListRepository.findByFeed(feed));
        saveOrUpdateTags(feed, dto.getTags());

        log.info("피드 수정 완료: feedId={}, userPublicId={}", feedId, feed.getUser().getPublicId());
        return mapToDto(feed);
    }

    /**
     * Feed 수정 전 조회 (프론트 수정용)
     */
    @Transactional(readOnly = true)
    public FeedResponseDto getFeedForUpdate(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드를 찾을 수 없습니다."));

        if (!feed.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return mapToDto(feed);
    }

    /**
     * 태그 저장 / 연결
     */
    @Transactional
    private void saveOrUpdateTags(Feed feed, List<String> tags) {
        if (tags == null || tags.isEmpty()) return;

        List<String> parsedTags = tags.stream()
                .flatMap(tag -> Arrays.stream(tag.split("#")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();

        for (String tagName : parsedTags) {
            Tag tagEntity = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

            if (!tagListRepository.existsByFeedAndTag(feed, tagEntity)) {
                tagListRepository.save(TagList.builder()
                        .feed(feed)
                        .tag(tagEntity)
                        .build());
            }
        }
    }

    /**
     * 피드 단건 조회
     */
    @Transactional
    public FeedResponseDto getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드를 찾을 수 없습니다."));
        feed.setView(feed.getView() + 1);
        feedRepository.save(feed);
        return mapToDto(feed);
    }

    /**
     * 피드 삭제
     */
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("피드를 찾을 수 없습니다."));
        if (!feed.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        imageRepository.deleteAll(imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L));
        tagListRepository.deleteAll(tagListRepository.findByFeed(feed));
        feedRepository.delete(feed);
    }



    /**
     * 무한 스크롤용 피드 조회
     */
    public Page<FeedResponseDto> getFeedsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Feed> feedPage = feedRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<FeedResponseDto> dtoList = feedPage.getContent().stream()
                .map(this::mapToDto)
                .toList();

        return new PageImpl<>(dtoList, pageable, feedPage.getTotalElements());
    }

    /**
     * Feed → DTO 변환 (공통)
     */
    private FeedResponseDto mapToDto(Feed feed) {
        List<Image> imageList = imageRepository.findByPostsIdAndTableTypesId(feed.getId(), 3L);
        List<TagList> tagList = tagListRepository.findByFeed(feed);
        return FeedResponseDto.of(feed, imageList, tagList);
    }

    /**
     * 로그인한 사용자의 피드 목록 조회 (최신순)
     */
    @Transactional(readOnly = true)
    public Page<FeedResponseDto> getMyFeeds(Long userId, int page, int size) {
        // createdAt 기준 내림차순 정렬 (최신순)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Feed> feedPage = feedRepository.findByUser_Id(userId, pageable);

        List<FeedResponseDto> dtoList = feedPage.getContent().stream()
                .map(this::mapToDto)
                .toList();

        log.info("내 피드 목록 조회 완료 (최신순): userId={}, totalFeeds={}", userId, feedPage.getTotalElements());

        return new PageImpl<>(dtoList, pageable, feedPage.getTotalElements());
    }

//  댓글 작성
    @Transactional
    public CommentResponseDto createComment(Long postId, Long userId, CommentCreateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Feed feed = feedRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("피드 없음"));

        TableType tableType = tableTypeRepository.findById(3L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        Comment parent = null;
        if (dto.getParentCommentsId() != null) {
            parent = commentRepository.findById(dto.getParentCommentsId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));
        }

        Comment comment = Comment.builder()
                .postsId(feed.getId())
                .usersId(user)
                .tableTypesId(tableType)
                .content(dto.getContent())
                .parentCommentsId(parent)
                .build();

        Comment saved = commentRepository.save(comment);

        // 알림은 별도 트랜잭션에서 실행 (Feed 트랜잭션에 영향 없음)
        try {
            alarmService.sendCommentAlarmAsync(
                    user.getUsername(),
                    postId,
                    dto.getParentCommentsId(),
                    tableType.getId()
            );
        } catch (Exception e) {
            log.warn("[FeedService] 알람 전송 실패 (무시됨): {}", e.getMessage());
        }

        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(user.getId(), 1L)
                .map(Image::getFilePath)
                .orElse(null);

        return CommentResponseDto.from(saved, profileImagePath);
    }


    /* 댓글 조회 */
    public Map<String, Object> getCommentsPage(Long feedId, int page, int size) {
        Page<Comment> parentComments =
                commentRepository.findByPostsIdAndParentCommentsIdIsNullWithUser(feedId, PageRequest.of(page, size));
        List<Comment> allComments = commentRepository.findByPostsIdWithUser(feedId);

        Set<Long> userIds = allComments.stream()
                .map(c -> c.getUsersId().getId())
                .collect(Collectors.toSet());

        Map<Long, String> profileMap = new HashMap<>();
        for (Long uid : userIds) {
            imageRepository.findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(uid, 1L)
                    .ifPresent(img -> profileMap.put(uid, img.getFilePath()));
        }

        List<CommentResponseDto> comments = parentComments.getContent().stream()
                .map(root -> CommentResponseDto.fromWithReplies(root, allComments, profileMap))
                .collect(Collectors.toList());

        return Map.of(
                "content", comments,
                "page", page + 1,
                "totalPages", parentComments.getTotalPages()
        );
    }

    /* 댓글 수정 */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        if (!comment.getUsersId().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 수정 가능");
        }

        comment.updateContent(newContent);
        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(userId, 1L)
                .map(Image::getFilePath)
                .orElse(null);
        return CommentResponseDto.from(comment, profileImagePath);
    }

    /* 댓글 삭제 */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        if (!comment.getUsersId().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 삭제 가능");
        }
        commentRepository.delete(comment);
    }


}
