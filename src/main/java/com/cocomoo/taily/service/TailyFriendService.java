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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                .tableType(tableType)
                .build();
        tailyFriendRepository.save(post);

        // 이미지 저장
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
                        .filePath("/uploads/taily-friends/" + newFileName) // 폴더만 다르게 설정
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(post.getId())
                        .user(null) // 프로필 이미지 아님
                        .tableTypesId(5L) // TailyFriend 게시판
                        .build();

                images.add(img);
            }
            imageRepository.saveAll(images);
        }
        log.info("게시글 작성 완료 id = {}, title = {}", post.getId(), post.getTitle());

        try {
            saveFilesWithLog(savedFileNames, requestDto.getImages(), "taily-friends");
        } catch (IOException e) {
            if (!images.isEmpty()) imageRepository.deleteAll(images); // DB 정합성 복원
            log.error("파일 저장 실패, 롤백 처리: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패, 트랜잭션 롤백", e);
        }
        List<ImageResponseDto> imageDtos = images.stream()
                .map(ImageResponseDto::from)
                .toList();

        return TailyFriendDetailResponseDto.from(post, false, imageDtos);
    }

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

    // 게시글 수정
    @Transactional
    public TailyFriendDetailResponseDto updateTailyFriend(Long postId, String username, TailyFriendCreateRequestDto dto) {
        // 게시글 조회
        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 내용 수정
        post.updatePost(dto.getTitle(), dto.getContent(), dto.getAddress());

        // 이미지 관련 로직
        List<Image> newImages = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();

        // 기존 이미지 DB 조회
        List<Image> existingImages = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 5L);

        // 기존 이미지 파일 삭제 및 DB 삭제
        if (!existingImages.isEmpty()) {
            imageRepository.deleteAll(existingImages);
            for (Image oldImg : existingImages) {
                try {
                    Path oldPath = Paths.get(System.getProperty("user.dir") + oldImg.getFilePath());
                    Files.deleteIfExists(oldPath);
                    log.info("기존 이미지 삭제 완료: {}", oldPath);
                } catch (IOException e) {
                    log.warn("기존 이미지 파일 삭제 실패: {}", oldImg.getFilePath(), e);
                }
            }
        }

        // 새 이미지 업로드
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile file : dto.getImages()) {
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

                newImages.add(img);
            }

            imageRepository.saveAll(newImages);
        }

        // 실제 파일 저장 (DB 성공 후)
        try {
            if (!savedFileNames.isEmpty()) {
                saveFilesWithLog(savedFileNames, dto.getImages(), "taily-friends");
            }
        } catch (IOException e) {
            if (!newImages.isEmpty()) imageRepository.deleteAll(newImages);
            log.error("파일 저장 실패, 롤백 처리: {}", e.getMessage(), e);
            throw new RuntimeException("파일 저장 실패, 트랜잭션 롤백", e);
        }

        // DTO 변환
        List<ImageResponseDto> imageDtos = (newImages.isEmpty()
                ? imageRepository.findByPostsIdAndTableTypesId(post.getId(), 5L)
                : newImages)
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("게시글 수정 완료 id = {}, title = {}", post.getId(), post.getTitle());

        return TailyFriendDetailResponseDto.from(post, false, imageDtos);
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

        boolean liked = likeRepository.existsByPostsIdAndTableTypeAndUserAndState(
                post.getId(), tableType, user, true
        );

        // 게시글에 연결된 이미지 조회
        List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(),5L)
                .stream()
                .map(ImageResponseDto::from)
                .toList();

        log.info("게시글 조회 성공: title={}", post.getTitle());

        return TailyFriendDetailResponseDto.from(post, liked, images);
    }

    // 테일리 프렌즈 전체 조회
    public TailyFriendPageResponseDto getTailyFriendsPage(int page, int size, String keyword) {
        Page<TailyFriend> postsPage = tailyFriendRepository.findAllWithUserAndKeyword(keyword, PageRequest.of(page, size));

        List<TailyFriendListResponseDto> posts = postsPage.stream()
                .map(post -> {
                    List<ImageResponseDto> images = imageRepository.findByPostsIdAndTableTypesId(post.getId(), 5L)
                            .stream()
                            .map(ImageResponseDto::from)
                            .toList();
                    return TailyFriendListResponseDto.from(post, images);
                })
                .toList();

        return TailyFriendPageResponseDto.builder()
                .data(posts)
                .totalCount(postsPage.getTotalElements())
                .build();
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
        TailyFriend post = tailyFriendRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        post.refreshLikeCount(likeCount);
        tailyFriendRepository.save(post);

        log.info("[TailyFriendService] 좋아요 상태: postId={}, liked={}, likeCount={}", postId, liked, likeCount);

        // 4. DTO 반환
        return new LikeResponseDto(liked, likeCount);
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

        // 알람 전송
        try {
            alarmService.sendCommentAlarm(username, postId, dto.getParentCommentsId(), tableType.getId());
            log.info("[TailyFriendService] 댓글 알람 전송 성공 → postId={}, username={}", postId, username);
        } catch (Exception e) {
            log.error("[TailyFriendService] 댓글 알람 전송 실패 → postId={}, username={}, 이유={}", postId, username, e.getMessage());
        }

        return CommentResponseDto.from(savedComment);
    }

    // 댓글 조회
    public Map<String, Object> getCommentsPage(Long postId, int page, int size) {
        Page<Comment> parentComments = commentRepository.findByPostsIdAndParentCommentsIdIsNullWithUser(postId, PageRequest.of(page, size));
        List<Comment> allComments = commentRepository.findByPostsIdWithUser(postId);

        List<CommentResponseDto> comments = parentComments.getContent().stream()
                .map(root -> CommentResponseDto.fromWithReplies(root, allComments))
                .collect(Collectors.toList());

        Map<String, Object> result = Map.of(
                "content", comments,
                "page", page + 1,                 // 프론트에서는 1부터 시작
                "totalPages", parentComments.getTotalPages()
        );
        return result;
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

    // 주소만 검색
    public List<TailyFriendAddressResponseDto> getAllAddresses() {
        return tailyFriendRepository.findAll().stream()
                .map(TailyFriendAddressResponseDto::from)
                .collect(Collectors.toList());
    }
}
