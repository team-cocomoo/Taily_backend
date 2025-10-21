package com.cocomoo.taily.service;

import com.cocomoo.taily.config.FileStorageProperties;
import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 통합 서비스 클래스 트랜잭션 처리
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileStorageProperties fileStorageProperties;
    private final UserRepository userRepository;

    /**
     * 이미지 업로드 (하위 폴더 지원)
     * - 저장 파일명: UUID_원본파일명
     * - tableTypesId == 1L → 프로필(usersId 기반)
     * - tableTypesId != 1L → 피드, 펫, 이벤트 등(postsId 기반)
     */
    /**
     * 이미지 업로드 (하위 폴더 지원)
     * - 저장 파일명: UUID_원본파일명
     * - tableTypesId == 1L → 프로필(usersId 기반)
     * - tableTypesId != 1L → 피드, 펫, 이벤트 등(postsId 기반)
     */
    @Transactional
    public List<Image> uploadImages(
            String subFolder,
            Long tableTypesId,
            Long usersId,
            Long postsId,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            log.warn("업로드할 파일이 없습니다. subFolder={}, tableTypesId={}, usersId={}, postsId={}",
                    subFolder, tableTypesId, usersId, postsId);
            return new ArrayList<>();
        }

        List<Image> savedImages = new ArrayList<>();

        try {
            // 1업로드 경로 설정
            String projectRoot = new File("").getAbsolutePath();
            String uploadPath = projectRoot + File.separator + fileStorageProperties.getUploadDir();

            // 하위 폴더 생성
            File uploadDir = new File(uploadPath, subFolder);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IOException("업로드 폴더 생성 실패: " + uploadDir.getAbsolutePath());
            }

            for (MultipartFile file : files) {
                // 유효성 검사
                if (file == null || file.isEmpty() || file.getSize() <= 0) {
                    log.warn("빈 파일이 포함되어 있어 건너뜁니다. 파일명={}",
                            file != null ? file.getOriginalFilename() : "null");
                    continue;
                }

                // 파일명 생성
                String uuid = UUID.randomUUID().toString();
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
                String newFileName = uuid + "_" + originalFileName;

                // 파일 저장
                File dest = new File(uploadDir, newFileName);
                file.transferTo(dest);

                // 상대 경로 (DB용)
                String filePath = "/uploads/" + subFolder + "/" + newFileName;

                // Image 엔티티 생성
                Image image = Image.builder()
                        .uuid(uuid)
                        .filePath(filePath) // 올바른 경로 유지
                        .fileSize(String.valueOf(file.getSize()))
                        .tableTypesId(tableTypesId)
                        .build();

                // 관계 설정
                if (tableTypesId == 1L) { // 프로필 이미지
                    if (usersId == null) {
                        throw new IllegalArgumentException("프로필 이미지 업로드 시 usersId는 필수입니다.");
                    }
                    User user = userRepository.findById(usersId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + usersId));
                    image.setUser(user);
                } else { // 피드, 펫 등
                    if (postsId == null) {
                        if (tableTypesId == 2L || tableTypesId == 4L) {
                            log.info("업로드 예외 허용: postsId가 null이지만 임시 업로드 처리됨 (tableTypesId={})", tableTypesId);
                        } else {
                            throw new IllegalArgumentException("기능 연관 이미지 업로드 시 postsId는 필수입니다. (tableTypesId=" + tableTypesId + ")");
                        }
                    }
                    image.setPostsId(postsId);
                }

                // 저장
                imageRepository.save(image);
                savedImages.add(image);
                log.info("이미지 저장 완료: [{}] {} ({} bytes)", subFolder, dest.getAbsolutePath(), file.getSize());
            }

        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 저장 실패", e);
        }

        return savedImages;
    }


    /**
     * 이미지 삭제
     * - tableTypesId == 1L → usersId 기준 (프로필)
     * - tableTypesId != 1L → postsId 기준 (피드, 펫 등)
     * - DB와 서버 파일 모두 삭제
     */
    @Transactional
    public void deleteImages(Long tableTypesId, Long usersId, Long postsId) {
        List<Image> images;

        // 1삭제 대상 조회
        if (tableTypesId == 1L) {
            if (usersId == null) {
                throw new IllegalArgumentException("프로필 이미지 삭제 시 usersId는 필수입니다.");
            }
            images = imageRepository.findByUserIdAndTableTypesId(usersId, tableTypesId);
        } else {
            if (postsId == null) {
                throw new IllegalArgumentException("기능성 이미지 삭제 시 postsId는 필수입니다.");
            }
            images = imageRepository.findByPostsIdAndTableTypesId(postsId, tableTypesId);
        }

        if (images.isEmpty()) {
            log.info("삭제할 이미지가 없습니다. tableTypesId={}, usersId={}, postsId={}", tableTypesId, usersId, postsId);
            return;
        }

        // 실제 파일 삭제
        for (Image image : images) {
            try {
                String relativePath = image.getFilePath().replaceFirst("^/+", ""); // "uploads/profile/..."
                String projectRoot = new File("").getAbsolutePath();
                File file = new File(projectRoot, relativePath);

                if (file.exists() && file.delete()) {
                    log.info(" 서버 파일 삭제 완료: {}", file.getAbsolutePath());
                } else {
                    log.warn("서버 파일 삭제 실패 또는 존재하지 않음: {}", file.getAbsolutePath());
                }

            } catch (Exception e) {
                log.error("파일 삭제 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        // DB 데이터 삭제
        imageRepository.deleteAll(images);
        log.info("🗑️ DB 이미지 레코드 삭제 완료: {}개 (tableTypesId={}, usersId={}, postsId={})",
                images.size(), tableTypesId, usersId, postsId);
    }

    /**
     * 이미지 조회
     * - tableTypesId == 1L → usersId 기준
     * - tableTypesId != 1L → postsId 기준
     */
    public List<Image> getImages(Long tableTypesId, Long usersId, Long postsId) {
        if (tableTypesId == 1L) {
            if (usersId == null) {
                throw new IllegalArgumentException("프로필 이미지 조회 시 usersId는 필수입니다.");
            }

            return imageRepository.findTopByUserIdAndTableTypesIdOrderByCreatedAtDesc(usersId, tableTypesId)
                    .map(List::of)
                    .orElseGet(List::of); // 없으면 빈 리스트 반환

        } else {
            if (postsId == null) {
                throw new IllegalArgumentException("기능성 이미지 조회 시 postsId는 필수입니다.");
            }
            return imageRepository.findByPostsIdAndTableTypesId(postsId, tableTypesId);
        }
    }
}
