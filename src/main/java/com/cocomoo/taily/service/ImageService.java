package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    /**
     * 이미지 업로드
     * tableTypesId:
     *  - 1L → usersId 기준 (프로필)
     *  - 나머지 → postsId 기준 (피드, 펫, 이벤트 등)
     */
    public List<Image> uploadImages(Long tableTypesId, Long usersId, Long postsId, List<MultipartFile> files) {
        User user = userRepository.findById(usersId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + usersId));

        List<Image> savedImages = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String uuid = UUID.randomUUID().toString();
                String filePath = fileStorageService.storeFile(file, uuid);

                Image image = Image.builder()
                        .uuid(uuid)
                        .filePath(filePath)
                        .fileSize(String.valueOf(file.getSize()))
                        .user(user)
                        .tableTypesId(tableTypesId)
                        .build();

                // tableTypesId에 따라 postsId 연결 여부 결정
                if (tableTypesId != 1L && postsId != null) {
                    image.setPostsId(postsId);
                }

                imageRepository.save(image);
                savedImages.add(image);

                log.info("이미지 업로드: tableType={}, userId={}, postsId={}, path={}",
                        tableTypesId, usersId, postsId, filePath);
            }
        }

        return savedImages;
    }

    /**
     * 이미지 조회
     * tableTypesId가 1L이면 usersId 기준,
     * 나머지는 postsId 기준으로 조회
     */
    public List<Image> getImages(Long tableTypesId, Long usersId, Long postsId) {
        if (tableTypesId == 1L) {
            return imageRepository.findByUserIdAndTableTypesId(usersId, tableTypesId);
        } else {
            return imageRepository.findByPostsIdAndTableTypesId(postsId, tableTypesId);
        }
    }
}
