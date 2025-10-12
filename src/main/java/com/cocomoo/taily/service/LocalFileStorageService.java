package com.cocomoo.taily.service;

import com.cocomoo.taily.service.FileStorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * LocalFileStorageService 클래스 파일
 * 로컬 서버에 파일 저장
 */
@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService {

    private final String uploadDir = "C:/taily/uploads/images";

    @Override
    public String storeFile(MultipartFile file, String uuid) {
        try {
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);

            String filename = uuid + "_" + file.getOriginalFilename();
            Path filePath = targetLocation.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            Path filePath = Paths.get(path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }
}
