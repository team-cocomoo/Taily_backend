/*
package com.cocomoo.taily.service;

import com.cocomoo.taily.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

*/
/**
 * 아마존 S3에 이미지 파일 저장시 사용
 *//*

@Service
@Profile("prod")
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName = "taily-bucket";

    @Override
    public String storeFile(MultipartFile file, String uuid) {
        try {
            String filename = uuid + "_" + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(bucketName, filename, file.getInputStream(), metadata);
            return amazonS3.getUrl(bucketName, filename).toString();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        String key = path.substring(path.lastIndexOf("/") + 1);
        amazonS3.deleteObject(bucketName, key);
    }
}
*/
