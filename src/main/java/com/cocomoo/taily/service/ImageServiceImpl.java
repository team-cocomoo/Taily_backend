package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.service.FileStorageService;
import com.cocomoo.taily.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final TableTypeRepository tableTypeRepository;

    @Override
    public List<Image> uploadImages(Long userId, Long tableTypeId, Long postsId, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음: " + tableTypeId));

        List<Image> images = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                String uuid = UUID.randomUUID().toString();
                String path = fileStorageService.storeFile(file, uuid);

                Image image = Image.builder()
                        .uuid(uuid)
                        .filePath(path)
                        .fileSize(String.valueOf(file.getSize()))
                        .postsId(postsId)
                        .user(user)
                        .tableType(tableType)
                        .build();

                imageRepository.save(image);
                images.add(image);
            }
        }
        return images;
    }

    @Override
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지 없음: " + imageId));
        fileStorageService.deleteFile(image.getFilePath());
        imageRepository.delete(image);
    }

    @Override
    public List<Image> getImagesByTableAndPost(Long tableTypeId, Long postsId) {
        TableType tableType = tableTypeRepository.findById(tableTypeId)
                .orElseThrow(() -> new IllegalArgumentException("TableType 없음: " + tableTypeId));
        return imageRepository.findByTableTypeAndPostsId(tableType, postsId);
    }
}
