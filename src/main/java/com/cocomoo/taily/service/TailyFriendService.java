package com.cocomoo.taily.service;

import com.cocomoo.taily.config.FileStorageProperties;
import com.cocomoo.taily.dto.common.comment.CommentCreateRequestDto;
import com.cocomoo.taily.dto.common.comment.CommentResponseDto;
import com.cocomoo.taily.dto.common.image.ImageResponseDto;
import com.cocomoo.taily.dto.common.like.LikeResponseDto;
import com.cocomoo.taily.dto.tailyFriends.*;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TailyFriendService {
    private final TailyFriendRepository tailyFriendRepository;
    private final UserService userService;
    private final LikeService likeService;
    private final AlarmService alarmService;
    private final TableTypeRepository tableTypeRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final FileStorageProperties fileStorageProperties;

    // ✅ 게시글 작성
    @Transactional
    public TailyFriendDetailResponseDto createTailyFriend(TailyFriendCreateRequestDto requestDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        log.info("게시글 작성: username = {}", username);

        TailyFriend post = TailyFriend.builder()
                .title(requestDto.getTitle())
                .address(requestDto.getAddress())
                .content(requestDto.getContent())
                .user(user)
                .tableType(tableType)
                .build();

        tailyFriendRepository.save(post);

        // ✅ 이미지 저장
        List<Image> images = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            for (MultipartFile file : requestDto.getImages()) {
                String uuid = UUID.randomUUID().toString();
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
                String newFileName = uuid + "_" + originalFileName;
                savedFileNames.add(newFileName);

                Image img = Image.builder()
                        .uuid(uuid)
                        .filePath("/uploads/taily-friends/" + newFileName)
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(post.getId())
                        .user(null)
                        .tableTypesId(5L)
                        .build();

                images.add(img);
            }
            imageRepository.saveAll(images);
        }

        try {
            saveFilesWithLog(savedFileNames, requestDto.getImages(), "taily-friends");
        } catch (IOException e) {
            if (!images.isEmpty()) imageRepository.deleteAll(images);
            log.error("파일 저장 실패, 롤백 처리: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패, 트랜잭션 롤백", e);
        }

        List<ImageResponseDto> imageDtos = images.stream()
                .map(ImageResponseDto::from)
                .toList();

        // ✅ 작성자 프로필 이미지 (table_types_id = 1L)
        Optional<Image> profileImageOpt = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(post.getUser().getId(), 1L);

        String profileImagePath = profileImageOpt.map(Image::getFilePath).orElse(null);

        return TailyFriendDetailResponseDto.from(post, false, imageDtos, profileImagePath);
    }

    // ✅ 파일 저장 유틸
    private void saveFilesWithLog(List<String> savedFileNames, List<MultipartFile> files, String subFolder) throws IOException {
        if (files == null || files.isEmpty()) return;

        String projectRoot = new File("").getAbsolutePath();
        String uploadPath = projectRoot + File.separator + fileStorageProperties.getUploadDir();

        File uploadDir = new File(uploadPath, subFolder);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("업로드 폴더 생성 실패: " + uploadDir.getAbsolutePath());
        }

        List<File> savedFiles = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileName = savedFileNames.get(i);
            File dest = new File(uploadDir, fileName);

            try {
                file.transferTo(dest);
                savedFiles.add(dest);
                log.info("파일 저장 성공: {}", dest.getAbsolutePath());
            } catch (IOException e) {
                for (File f : savedFiles) {
                    if (f.exists()) f.delete();
                }
                throw e;
            }
        }
    }

    @Transactional
    public TailyFriendDetailResponseDto updateTailyFriend(
            Long postId,
            String username,
            TailyFriendUpdateRequestDto dto,
            List<MultipartFile> newImages
    ) {
        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        post.updatePost(dto.getTitle(), dto.getContent(), dto.getAddress());

        List<Image> existingImages = imageRepository.findByPostsIdAndTableTypesId(postId, 5L);

        // 유지할 이미지 경로 목록
        List<String> keepPaths = dto.getExistingImagePaths() != null ? dto.getExistingImagePaths() : List.of();

        // 삭제 대상 = 기존 이미지 중 유지 목록에 없는 것
        List<Image> deleteTargets = existingImages.stream()
                .filter(img -> !keepPaths.contains(img.getFilePath()))
                .toList();

        if (!deleteTargets.isEmpty()) {
            imageRepository.deleteAll(deleteTargets);
            for (Image oldImg : deleteTargets) {
                try {
                    Path oldPath = Paths.get(System.getProperty("user.dir") + oldImg.getFilePath());
                    Files.deleteIfExists(oldPath);
                    log.info("삭제된 이미지 파일: {}", oldImg.getFilePath());
                } catch (IOException e) {
                    log.warn("이미지 파일 삭제 실패: {}", oldImg.getFilePath(), e);
                }
            }
        }

        List<Image> newImageEntities = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                String uuid = UUID.randomUUID().toString();
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
                String newFileName = uuid + "_" + originalFileName;

                savedFileNames.add(newFileName);

                Image newImg = Image.builder()
                        .uuid(uuid)
                        .filePath("/uploads/taily-friends/" + newFileName)
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(post.getId())
                        .user(null)
                        .tableTypesId(5L)
                        .build();

                newImageEntities.add(newImg);
            }
            imageRepository.saveAll(newImageEntities);
        }

        try {
            if (!savedFileNames.isEmpty())
                saveFilesWithLog(savedFileNames, newImages, "taily-friends");
        } catch (IOException e) {
            if (!newImageEntities.isEmpty()) imageRepository.deleteAll(newImageEntities);
            throw new RuntimeException("파일 저장 실패", e);
        }

        List<ImageResponseDto> imageDtos =
                imageRepository.findByPostsIdAndTableTypesId(postId, 5L)
                        .stream().map(ImageResponseDto::from).toList();

        Optional<Image> profileImageOpt =
                imageRepository.findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(post.getUser().getId(), 1L);
        String profileImagePath = profileImageOpt.map(Image::getFilePath).orElse(null);

        log.info("게시글 수정 완료: {}, 유지 {}, 삭제 {}, 추가 {}",
                postId, keepPaths.size(), deleteTargets.size(), newImageEntities.size());

        return TailyFriendDetailResponseDto.from(post, false, imageDtos, profileImagePath);
    }


    // 게시글 삭제
    @Transactional
    public void deleteTailyFriend(Long postId, String username) {
        TailyFriend post = tailyFriendRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 검증
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        tailyFriendRepository.delete(post);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementViewCount(Long id) {
        tailyFriendRepository.incrementViewCount(id);
    }


    // ✅ 상세 조회
    @Transactional
    public TailyFriendDetailResponseDto getTailyFriendById(Long postId, String username) {
        incrementViewCount(postId);

        TailyFriend post = tailyFriendRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TableType tableType = tableTypeRepository.findById(5L)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        boolean liked = likeRepository.existsByPostsIdAndTableTypeAndUserAndState(post.getId(), tableType, user, true);

        List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 5L)
                .stream().map(ImageResponseDto::from).toList();

        // ✅ 작성자 프로필 이미지
        Optional<Image> profileImageOpt = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(post.getUser().getId(), 1L);

        String profileImagePath = profileImageOpt.map(Image::getFilePath).orElse(null);

        return TailyFriendDetailResponseDto.from(post, liked, images, profileImagePath);
    }

    // 테일리 프렌즈 전체 조회
    public TailyFriendPageResponseDto getTailyFriendsPage(int page, int size, String keyword) {
        Page<TailyFriend> postsPage = tailyFriendRepository.findAllWithUserAndKeyword(keyword, PageRequest.of(page, size));

        List<TailyFriendListResponseDto> posts = postsPage.stream().map(post -> {
            List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 5L).stream().map(ImageResponseDto::from).toList();
            return TailyFriendListResponseDto.from(post, images);
        }).toList();

        return TailyFriendPageResponseDto.builder().data(posts).totalCount(postsPage.getTotalElements()).build();
    }

    // 좋아요 상태 변화
    @Transactional
    public LikeResponseDto toggleLike(Long postId, String username) {
        Long tableTypeId = 5L; // TailyFriend

        // 1. Like 테이블 상태 토글
        boolean liked = likeService.toggleLike(postId, username, tableTypeId);

        // 2. 최신 좋아요 수 가져오기
        Long likeCount = likeService.getLikeCount(postId, tableTypeId);

        // 3. TailyFriend 엔티티에 반영
        TailyFriend post = tailyFriendRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        post.refreshLikeCount(likeCount);
        tailyFriendRepository.save(post);

        log.info("[TailyFriendService] 좋아요 상태: postId={}, liked={}, likeCount={}", postId, liked, likeCount);

        // 4. DTO 반환
        return new LikeResponseDto(liked, likeCount);
    }


    // 댓글 작성
    @Transactional
    public CommentResponseDto createComment(Long postId, String username, CommentCreateRequestDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        TailyFriend post = tailyFriendRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        TableType tableType = tableTypeRepository.findById(5L) // TailyFriend = 5
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음"));

        Comment parent = null;
        if (dto.getParentCommentsId() != null) {
            parent = commentRepository.findById(dto.getParentCommentsId()).orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));
        }

        Comment comment = Comment.builder().postsId(post.getId()).usersId(user).tableTypesId(tableType).content(dto.getContent()).parentCommentsId(parent).build();

        Comment savedComment = commentRepository.save(comment);

        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(user.getId(), 1L)
                .map(image -> image.getFilePath())
                .orElse(null);
        // 알람 전송
        try {
            alarmService.sendCommentAlarm(username, postId, dto.getParentCommentsId(), tableType.getId());
            log.info("[TailyFriendService] 댓글 알람 전송 성공 → postId={}, username={}", postId, username);
        } catch (Exception e) {
            log.error("[TailyFriendService] 댓글 알람 전송 실패 → postId={}, username={}, 이유={}", postId, username, e.getMessage());
        }

        return CommentResponseDto.from(savedComment, profileImagePath);
    }

    // 댓글 조회
    public Map<String, Object> getCommentsPage(Long postId, int page, int size) {
        Page<Comment> parentComments = commentRepository.findByPostsIdAndParentCommentsIdIsNullWithUser(postId, PageRequest.of(page, size));
        List<Comment> allComments = commentRepository.findByPostsIdWithUser(postId);

        // ✅ 모든 댓글 작성자 ID 모으기
        Set<Long> userIds = allComments.stream()
                .map(c -> c.getUsersId().getId())
                .collect(Collectors.toSet());

        // ✅ 사용자별 프로필 이미지 미리 조회 (N+1 방지)
        Map<Long, String> profileMap = new HashMap<>();
        for (Long userId : userIds) {
            imageRepository.findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(userId, 1L)
                    .ifPresent(image -> profileMap.put(userId, image.getFilePath()));
        }

        // ✅ 댓글 + 대댓글 트리 구조 변환
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
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String username, String newContent) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 작성자 검증
        if (!comment.getUsersId().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        String profileImagePath = imageRepository
                .findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(user.getId(), 1L)
                .map(image -> image.getFilePath())
                .orElse(null);
        comment.updateContent(newContent);

        return CommentResponseDto.from(comment, profileImagePath);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 작성자 검증
        if (!comment.getUsersId().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    // 주소만 검색
    public List<TailyFriendAddressResponseDto> getAllAddresses() {
        return tailyFriendRepository.findAll().stream().map(TailyFriendAddressResponseDto::from).collect(Collectors.toList());
    }
}
