package com.cocomoo.taily.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/** 서버에서 저장한 파일을 URL로 접근 가능하게 한다. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    public WebConfig(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트 기준 절대 경로
        String projectRoot = new File("").getAbsolutePath();
        String uploadPath = projectRoot + File.separator + fileStorageProperties.getUploadDir() + File.separator;

        // /uploads/** 요청 -> 실제 서버 uploads 폴더와 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath); // file: 접두사 필수
    }
}
