package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.common.like.LikeResponseDto;
import com.cocomoo.taily.dto.walkPaths.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
@Slf4j
public class WalkPathService {
    private final WalkPathRepository walkPathRepository;
    private final WalkPathRoutesRepository walkPathRoutesRepository;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FileUploadService fileUploadService;
    private final AlarmService alarmService;

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

        Long likeCount = likeRepository.countByPostsIdAndTableTypeAndState(post.getId(),tableType,true);
        post.refreshLikeCount(likeCount);

        boolean liked = likeRepository.existsByPostsIdAndTableTypeAndUserAndState(
                post.getId(), tableType, user, true
        );
        // 게시글에 연결된 경로지점들 조회
        List<WalkPathRouteResponseDto> routes = walkPathRepository.findByWalkPathId(postId)
                .stream()
                .map(WalkPathRouteResponseDto::from)
                .toList();


        // 게시글에 연결된 이미지 조회
        List<String> imagePaths = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 6L)
                .stream()
                .map(Image::getFilePath)
                .filter(Objects::nonNull)
                .toList();

        return WalkPathDetailResponseDto.from(post,liked,imagePaths,routes);
    }

    //게시물 생성
    @Transactional(readOnly = false)
    public WalkPathDetailResponseDto createWalkPath(WalkPathCreateRequestDto requestDto, String username, List<MultipartFile> images) {
        //작성자 조회
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        //Tabletype 명시
        TableType tableType = tableTypeRepository.findById(6L) // WalkPath = 6
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        //Post 생성
        WalkPath walkPath = WalkPath.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .tableType(tableType)
                .user(author) // ManyToOne 관계 설정
                .build();

        //db에 저장
        WalkPath savedWalkPath = walkPathRepository.save(walkPath);

        //경로 지점들 저장
        List<WalkPathRoute> savedRoutes = new ArrayList<>();
        if (requestDto.getRoutes() != null && !requestDto.getRoutes().isEmpty()) {
            List<WalkPathRoute> routeEntities = requestDto.getRoutes().stream()
                    .map(routeDto -> WalkPathRoute.builder()
                            .address(routeDto.getAddress())
                            .orderNo(routeDto.getOrderNo())
                            .walkPath(savedWalkPath)   // 부모 엔티티 지정
                            .build())
                    .toList();

            savedRoutes = walkPathRoutesRepository.saveAll(routeEntities);
            log.info("총 {}개의 경로 지점이 저장되었습니다.", savedRoutes.size());
        }

        // 이미지 저장
        List<Image> imageEntities = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();

        List<String> imaged = null;
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue; // 빈 파일은 건너뛰기

                // (1) 파일명 및 경로 생성
                String uuid = UUID.randomUUID().toString();
                String originalName = StringUtils.cleanPath(file.getOriginalFilename());
                String newFileName = uuid + "_" + originalName;

                // (2) 저장 디렉토리 설정 (절대 경로)
                String uploadDir = System.getProperty("user.dir") + "/uploads/walkpath/";
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    boolean created = uploadPath.mkdirs();
                    if (created) {
                        log.info("이미지 업로드 폴더 생성 완료: {}", uploadPath.getAbsolutePath());
                    } else {
                        log.warn("이미지 업로드 폴더 생성 실패 또는 이미 존재: {}", uploadPath.getAbsolutePath());
                    }
                }

                // (3) 실제 파일 저장
                try {
                    File destination = new File(uploadPath, newFileName);
                    file.transferTo(destination);
                    log.info("✅ 이미지 저장 완료: {}", destination.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패: " + originalName, e);
                }

                // (4) DB 엔티티 생성
                String imageUrl = "http://localhost:8080/uploads/walkpath/" + newFileName;
                Image image = Image.builder()
                        .uuid(uuid)
                        .filePath(imageUrl) // 웹에서 접근 가능한 경로
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(savedWalkPath.getId())
                        .user(author)
                        .tableTypesId(6L) // WalkPath
                        .build();

                imageEntities.add(image);
                imagePaths.add(image.getFilePath());
            }
            //(5) DB저장
            imageRepository.saveAll(imageEntities);
            imageRepository.flush();
        }
        // ✅ 경로를 DTO로 변환
        List<WalkPathRouteResponseDto> routeDtos = savedRoutes.stream()
                .map(WalkPathRouteResponseDto::from)
                .toList();
        log.info("게시글 작성 완료 id = {}, title = {}", savedWalkPath.getId(), savedWalkPath.getTitle());

        return WalkPathDetailResponseDto.from(savedWalkPath, false, imagePaths,routeDtos);
    }

    //전체 게시물 목록으로 조회
    public List<WalkPathListResponseDto> findAllPostList(int page, int size) {

        Page<WalkPath> posts = walkPathRepository.findAllWithUser(PageRequest.of(page, size));

        return posts.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 6L)
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return WalkPathListResponseDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .view(post.getView())
                            .images(images)
                            .createdAt(post.getCreatedAt())
                            .nickname(post.getUser() != null ? post.getUser().getNickname() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 게시글 수정
    @Transactional
    public WalkPathDetailResponseDto updateWalkPath(Long postId, String username, WalkPathUpdateRequestDto requestDto, List<MultipartFile> images){
        //기존 게시글 조회
        WalkPath post = walkPathRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        //기존 게시글 제목 , 내용 업데이트
        post.update(requestDto.getTitle(), requestDto.getContent());

        //작성자 조회
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 5️⃣ 기존 경로 삭제 후 새로 저장
        walkPathRoutesRepository.deleteByWalkPath_Id(post.getId());
        List<WalkPathRoute> newRoutes = new ArrayList<>();
        if (requestDto.getRoutes() != null && !requestDto.getRoutes().isEmpty()) {
            for (WalkPathRouteRequestDto routeDto : requestDto.getRoutes()) {
                WalkPathRoute route = new WalkPathRoute();
                route.setAddress(routeDto.getAddress());
                route.setOrderNo(routeDto.getOrderNo());
                route.setWalkPath(post);  // 기존 게시글과 연결
                newRoutes.add(route);
            }
            walkPathRoutesRepository.saveAll(newRoutes);
        }

        //기존 이미지 삭제
        List<Image> existingImages = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 6L);
        if (!existingImages.isEmpty()) {
            imageRepository.deleteAll(existingImages);
            imageRepository.flush();
            log.info("기존 이미지 {}개 삭제 완료", existingImages.size());
        }

        // 이미지 새로 업로드
        List<Image> imageEntities = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();
        List<String> imaged = null;
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue; // 빈 파일은 건너뛰기

                try {
                    // (1) FileUploadService를 이용하여 파일 저장 및 URL 반환
                    String filePath = fileUploadService.saveFile(file);

                    // (2) UUID 생성
                    String uuid = UUID.randomUUID().toString();

                    // (3) Image 엔티티 생성 (DB 저장용)
                    Image image = new Image();
                    image.setUuid(UUID.randomUUID().toString());
                    image.setFilePath(filePath);
                    image.setFileSize(String.valueOf(file.getSize()));
                    image.setPostsId(post.getId());
                    image.setUser(author);
                    image.setTableTypesId(6L); // WalkPath

                    imageEntities.add(image);
                    imagePaths.add(filePath);
                    log.info("✅ 이미지 업로드 성공: {}", filePath);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패: " + file.getOriginalFilename(), e);
                }
            }
            imageRepository.saveAll(imageEntities);
            imageRepository.flush();
        }

        // ✅ 경로를 DTO로 변환
        List<WalkPathRouteResponseDto> routeDtos = newRoutes.stream()
                .map(WalkPathRouteResponseDto::from)
                .toList();
//        log.info("게시글 작성 완료 id = {}, title = {}", post.getId(), post.getTitle());

        return WalkPathDetailResponseDto.from(post, false, imagePaths,routeDtos);

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
        // 자식 엔티티(경로 지점) 먼저 삭제
        walkPathRoutesRepository.deleteByWalkPath_Id(postId);

        // 부모 엔티티(게시글) 삭제
        walkPathRepository.delete(post);

        walkPathRepository.delete(post);
    }

    //좋아요 상태 변화
    @Transactional
    public LikeResponseDto toggleLike(Long postId, String username) {
        Long tableTypeId = 6L; // TailyFriend

        // 1. Like 테이블 상태 토글
        boolean liked = likeService.toggleLike(postId, username, tableTypeId);

        // 2. 최신 좋아요 수 가져오기
        Long likeCount = likeService.getLikeCount(postId, tableTypeId);

        // 3. TailyFriend 엔티티에 반영
        WalkPath post = walkPathRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        post.refreshLikeCount(likeCount);
//        walkPathRepository.save(post);

        // 4. DTO 반환
        return new LikeResponseDto(liked, likeCount);
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
        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(user.getId(), 1L)
                .map(image -> image.getFilePath())
                .orElse(null);

        // 알람 전송
        try {
            alarmService.sendCommentAlarm(username, post.getId(), dto.getParentCommentsId(), tableType.getId());
            log.info("[WalkPathService] 댓글 알람 전송 성공 → postId={}, username={}", post.getId(), username);
        } catch (Exception e) {
            log.error("[WalkPathService] 댓글 알람 전송 실패 → postId={}, username={}, 이유={}", post.getId(), username, e.getMessage());
        }

        return CommentResponseDto.from(savedComment, profileImagePath);
    }
    //댓글 조회
    public Map<String, Object> getCommentsPage(Long postId, int page, int size) {
        Page<Comment> parentComments = commentRepository.findByPostsIdAndParentCommentsIdIsNullWithUser(postId, PageRequest.of(page, size));
        List<Comment> allComments = commentRepository.findByPostsIdWithUser(postId);

        Set<Long> userIds = allComments.stream()
                .map(c -> c.getUsersId().getId())
                .collect(Collectors.toSet());

        Map<Long, String> profileMap = new HashMap<>();
        for (Long userId : userIds) {
            imageRepository.findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(userId, 1L)
                    .ifPresent(image -> profileMap.put(userId, image.getFilePath()));
        }

        List<CommentResponseDto> comments = parentComments.getContent().stream()
                .map(root -> CommentResponseDto.fromWithReplies(root, allComments, profileMap))
                .collect(Collectors.toList());

        return Map.of(
                "content", comments,
                "page", page + 1, // 프론트는 1부터 시작
                "totalPages", parentComments.getTotalPages()
        );
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long commentId, String username, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 작성자 검증
        if (!comment.getUsersId().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(comment.getUsersId().getId(), 1L)
                .map(image -> image.getFilePath())
                .orElse(null);
        comment.updateContent(newContent);
        return CommentResponseDto.from(comment, profileImagePath);
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
                    // 이미지 조회
                    List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 6L)
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();

                    // ✅ User 엔티티 제거, 닉네임만 전달
                    return WalkPathListResponseDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .view(post.getView())
                            .images(images)
                            .createdAt(post.getCreatedAt())
                            .nickname(post.getUser() != null ? post.getUser().getNickname() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}