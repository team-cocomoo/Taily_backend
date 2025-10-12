package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.repository.ImageRepository;
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

    @Override
    public List<Image> uploadImages(Long userId, TableType tableType, Long postsId, List<MultipartFile> files) {
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
                        .userId(userId)
                        .tableTypeId(tableType)
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
    public List<Image> getImagesByTableAndPost(TableType tableType, Long postsId) {
        return imageRepository.findByTableTypeIdAndPostsId(tableType, postsId);
    }
}
