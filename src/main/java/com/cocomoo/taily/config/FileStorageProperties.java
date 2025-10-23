package com.cocomoo.taily.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
// application.properties의 키를 자바 객체로 매핑

/**
 * application.properties의 업로드 폴더 경로를 코드에서 가져오는 클래스
 */
public class FileStorageProperties {
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}