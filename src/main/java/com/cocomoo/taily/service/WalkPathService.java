package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendListResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathCreateRequestDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathListResponseDto;
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

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
@Slf4j
public class WalkPathService {
    private final WalkPathRepository walkPathRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    //게시물 상세 조회
    @Transactional
    public WalkPathDetailResponseDto getWalkPathById(Long postId, String username) {
        log.info("게시글 상세 조회 : id = {}", postId);

        WalkPath post = walkPathRepository.findByIdWithUser(postId).orElseThrow(() -> {
            log.error("게시글 조회 실패: id={}", postId);
            return new IllegalArgumentException("존재하지 않는 게시글입니다.");
        });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(6L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        log.info("게시글 조회 성공: title={}", post.getTitle());

        post.increaseView();

        boolean liked = likeRepository.existsByPostsIdAndTableTypeAndUserAndState(
                post.getId(), tableType, user, true
        );
        // 게시글에 연결된 이미지 조회
        List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        return WalkPathDetailResponseDto.from(post,liked,images);
    }

    //게시물 생성
    @Transactional
    public WalkPathDetailResponseDto createWalkPath(WalkPathCreateRequestDto requestDto, String username) {
        //작성자 조회
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        //tabletype 명시
        TableType tableType = tableTypeRepository.findById(6L) // WalkPath = 6
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        //Post 생성
        WalkPath walkPath = WalkPath.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .user(author) // ManyToOne 관계 설정
                .build();
        //db에 저장
        WalkPath savedWalkPath = walkPathRepository.save(walkPath);

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
                                .postsId(savedWalkPath.getId())
                                .user(author)
                                .tableType(tableType)
                                .build();
                    })
                    .toList();

            imageRepository.saveAll(imageEntities);

            // DTO 변환
            images = imageEntities.stream()
                    .map(ImageResponseDto::from)
                    .toList();


        }
        log.info("게시글 작성 완료 id = {}, title = {}", savedWalkPath.getId(), savedWalkPath.getTitle());

        return WalkPathDetailResponseDto.from(savedWalkPath,false,images);
    }

    //전체 게시물 목록으로 조회
    public List<WalkPathListResponseDto> findAllPostList(int page, int size) {

        Page<WalkPath> posts = walkPathRepository.findAllWithUser(PageRequest.of(page, size));

        return posts.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return WalkPathListResponseDto.from(post, images);
                })
                .collect(Collectors.toList());
    }

    // 게시글 수정
    @Transactional
    public WalkPathDetailResponseDto updateWalkPath(Long postId, String username, WalkPathCreateRequestDto dto){
        WalkPath post = walkPathRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        post.updatePost(dto.getTitle(), dto.getContent());

        // 이미지 수정
        List<ImageResponseDto> images = new ArrayList<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            User user = post.getUser();
            TableType tableType = tableTypeRepository.findById(6L)
                    .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

            List<Image> imageEntities = dto.getImages().stream()
                    .map(imgDto -> {
                        String uuid = UUID.randomUUID().toString(); // 이미지별 고유 UUID
                        return Image.builder()
                                .uuid(uuid)
                                .filePath(imgDto.getFilePath())
                                .fileSize(imgDto.getFileSize())
                                .postsId(post.getId())
                                .user(user)
                                .tableType(tableType)
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
        return WalkPathDetailResponseDto.from(post,false,images);
    }

    // 게시글 삭제
    @Transactional
    public void deleteWalkPath(Long postId, String username) {
        WalkPath post = walkPathRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 검증
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        walkPathRepository.delete(post);
    }

    //좋아요 상태 변화
    public void toggleLike(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        WalkPath post = walkPathRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(6L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        boolean isLiked = likeService.toggleLike(postId, username, 6L);

        if (isLiked) post.increaseLike();
        else post.decreaseLike();
    }
    //댓글 작성
    @Transactional
    public CommentResponseDto createComment(Long id, String username, CommentCreateRequestDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        WalkPath post = walkPathRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(6L) // WalkPath= 6
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
    //댓글 조회
    public List<CommentResponseDto> getCommentsPage(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // page: 0부터 시작
        Page<Comment> parentComments = commentRepository.findByPostsIdAndParentCommentsIdIsNull(postId, pageable);

        List<Comment> allComments = commentRepository.findByPostsId(postId); // 전체 댓글 (대댓글 포함)

        return parentComments.getContent().stream()
                .map(root -> CommentResponseDto.fromWithReplies(root, allComments))
                .collect(Collectors.toList());
    }
    // 댓글 수정
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
    public List<WalkPathListResponseDto> searchWalkPathsPage(String keyword, int page, int size) {
        Page<WalkPath> posts = walkPathRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        return posts.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsId(post.getId())
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return WalkPathListResponseDto.from(post, images);
                })
                .collect(Collectors.toList());
    }
}