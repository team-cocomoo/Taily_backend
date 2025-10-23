package com.cocomoo.taily.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장을 하는 FileStorageService 인터페이스 파일
 */
public interface FileStorageService {
    String storeFile(MultipartFile file, String uuid);
    void deleteFile(String path);
}
