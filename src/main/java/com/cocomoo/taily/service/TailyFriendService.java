package com.cocomoo.taily.service;

import com.cocomoo.taily.repository.TailyFriendRepository;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.common.image.ImageRequestDto;
import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendAddressResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendCreateRequestDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendListResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TailyFriendService {
    private final TailyFriendRepository tailyFriendRepository;
    private final UserService userService;
    private final TableTypeRepository tableTypeRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;

    // 게시글 작성
    @Transactional
    public TailyFriendDetailResponseDto createTailyFriend(TailyFriendCreateRequestDto requestDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L) // TailyFriend = 5
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        log.info("게시글 작성: username = {}", username);

        TailyFriend post = TailyFriend.builder()
                .title(requestDto.getTitle())
                .address(requestDto.getAddress())
                .content(requestDto.getContent())
                .user(user)
                .build();
        TailyFriend savedPost = tailyFriendRepository.save(post);

        // 이미지 저장
        List<ImageResponseDto> images = new ArrayList<>();
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<Image> imageEntities = requestDto.getImages().stream()
                    .map(imgDto -> {
                        String uuid = UUID.randomUUID().toString(); // 이미지별 고유 UUID 생성
                        return Image.builder()
                                .uuid(uuid)
                                .filePath(imgDto.getFilePath())
                                .fileSize(imgDto.getFileSize())
                                .postsId(savedPost.getId())
                                .usersId(user)
                                .tableTypeId(tableType)
                                .build();
                    })
                    .toList();

            imageRepository.saveAll(imageEntities);

            // DTO 변환
            images = imageEntities.stream()
                    .map(ImageResponseDto::from)
                    .toList();
        }

        log.info("게시글 작성 완료 id = {}, title = {}", savedPost.getId(), savedPost.getTitle());


        return TailyFriendDetailResponseDto.from(savedPost, false, images);
    }

    // 게시글 수정
    @Transactional
    public TailyFriendDetailResponseDto updateTailyFriend(Long postId, String username, TailyFriendCreateRequestDto dto) {
        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));


        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        post.updatePost(dto.getTitle(), dto.getAddress(), dto.getContent());

        // 이미지 수정
        List<ImageResponseDto> images = new ArrayList<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            User user = post.getUser();
            TableType tableType = tableTypeRepository.findById(5L)
                    .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

            List<Image> imageEntities = dto.getImages().stream()
                    .map(imgDto -> {
                        String uuid = UUID.randomUUID().toString(); // 이미지별 고유 UUID
                        return Image.builder()
                                .uuid(uuid)
                                .filePath(imgDto.getFilePath())
                                .fileSize(imgDto.getFileSize())
                                .postsId(post.getId())
                                .usersId(user)
                                .tableTypeId(tableType)
                                .build();
                    })
                    .toList();

            imageRepository.saveAll(imageEntities); // 배치 저장

            images = imageEntities.stream()
                    .map(ImageResponseDto::from)
                    .toList();
        } else {
            // 기존 이미지 조회만
            images = imageRepository.findByPostsId(post.getId())
                    .stream()
                    .map(ImageResponseDto::from)
                    .toList();
        }

        return TailyFriendDetailResponseDto.from(post, false, images);
    }

    // 게시글 삭제
    @Transactional
    public void deleteTailyFriend(Long postId, String username) {
        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 검증
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        tailyFriendRepository.delete(post);
    }

    // 테일리 프렌즈 상세 조회
    @Transactional
    public TailyFriendDetailResponseDto getTailyFriendById(Long postId, String username) {
        log.info("게시글 상세 조회 : id = {}", postId);

        TailyFriend post = tailyFriendRepository.findByIdWithUser(postId).orElseThrow(() -> {
            log.error("게시글 조회 실패: id={}", postId);
            return new IllegalArgumentException("존재하지 않는 게시글입니다.");
        });

        // 조회수 증가
        post.increaseView();

        // 현재 사용자의 좋아요 상태 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        boolean liked = likeRepository.existsByPostsIdAndTableTypesIdAndUsersIdAndState(
                post.getId(), tableType, user, true
        );

        // 게시글에 연결된 이미지 조회
        List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("게시글 조회 성공: title={}", post.getTitle());

        return TailyFriendDetailResponseDto.from(post, liked, images);
    }

    // 테일리 프렌즈 전체 조회
    public List<TailyFriendListResponseDto> getTailyFriendsPage(int page, int size) {
        Page<TailyFriend> posts = tailyFriendRepository.findAllWithUser(PageRequest.of(page, size));

        return posts.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return TailyFriendListResponseDto.from(post, images);
                })
                .collect(Collectors.toList());
    }

    // 좋아요 상태 변화
    @Transactional
    public void toggleLike(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        Like like = likeRepository.findByPostsIdAndTableTypesIdAndUsersIdAndState(
                post.getId(), tableType, user, true
        ).orElse(null);

        if (like == null) {
            // 좋아요 생성
            likeRepository.save(
                    Like.builder()
                            .postsId(post.getId())
                            .usersId(user)
                            .tableTypesId(tableType)
                            .state(true)
                            .build()
            );
            post.increaseLike();
        } else {
            // 좋아요 취소
            like.toggle();
            post.decreaseLike();
        }
    }

    // 댓글 작성
    @Transactional
    public CommentResponseDto createComment(Long postId, String username, CommentCreateRequestDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(5L) // TailyFriend = 5
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        Comment parent = null;
        if (dto.getParentCommentsId() != null) {
            parent = commentRepository.findById(dto.getParentCommentsId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));
        }

        Comment comment = Comment.builder()
                .postsId(post.getId())
                .usersId(user)
                .tableTypesId(tableType)
                .content(dto.getContent())
                .parentCommentsId(parent)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentResponseDto.from(savedComment);
    }

    // 댓글 조회
    public List<CommentResponseDto> getCommentsPage(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // page: 0부터 시작
        Page<Comment> parentComments = commentRepository.findByPostsIdAndParentCommentsIdIsNull(postId, pageable);

        List<Comment> allComments = commentRepository.findByPostsId(postId); // 전체 댓글 (대댓글 포함)

        return parentComments.getContent().stream()
                .map(root -> CommentResponseDto.fromWithReplies(root, allComments))
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String username, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 작성자 검증
        if (!comment.getUsersId().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        comment.updateContent(newContent);

        return CommentResponseDto.from(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 작성자 검증
        if (!comment.getUsersId().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    // 검색 기능
    public List<TailyFriendListResponseDto> searchTailyFriendsPage(String keyword, int page, int size) {
        Page<TailyFriend> posts = tailyFriendRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        return posts.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return TailyFriendListResponseDto.from(post, images);
                })
                .collect(Collectors.toList());
    }

    // 주소만 검색
    public List<TailyFriendAddressResponseDto> getAllAddresses() {
        List<String> addresses = tailyFriendRepository.findAllAddresses();
        return addresses.stream()
                .map(TailyFriendAddressResponseDto::from)
                .collect(Collectors.toList());
    }
}
