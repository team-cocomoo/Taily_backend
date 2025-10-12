package com.cocomoo.taily.service;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.entity.TableType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<Image> uploadImages(Long userId, TableType tableType, Long postsId, List<MultipartFile> files);
    void deleteImage(Long imageId);
    List<Image> getImagesByTableAndPost(TableType tableType, Long postsId);
}
