package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<Image> uploadImages(Long userId, Long tableType, Long postsId, List<MultipartFile> files);
    void deleteImage(Long imageId);
    List<Image> getImagesByTableAndPost(Long tableType, Long postsId);
}
