package com.cocomoo.taily.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {
    private final String uploadDir = "uploads/";

    public String saveFile(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));

        String uuid = UUID.randomUUID().toString();
        String filename = uuid + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename; // DB에 저장할 URL
    }
}
