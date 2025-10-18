package com.cocomoo.taily.controller;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 업로드
     * - tableTypesId=1 → usersId 기준
     * - tableTypesId!=1 → postsId 기준
     */
    @PostMapping("/upload")
    public ResponseEntity<List<Image>> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("tableTypesId") Long tableTypesId,
            @RequestParam(value = "postsId", required = false) Long postsId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        Long usersId = userDetails.getUserId();
        List<Image> images = imageService.uploadImages(tableTypesId, usersId, postsId, files);
        return ResponseEntity.ok(images);
    }

    /**
     * 이미지 조회
     * - tableTypesId=1 → usersId 기준
     * - tableTypesId!=1 → postsId 기준
     */
    @GetMapping
    public ResponseEntity<List<Image>> getImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("tableTypesId") Long tableTypesId,
            @RequestParam(value = "postsId", required = false) Long postsId
    ) {
        Long usersId = userDetails.getUserId();
        List<Image> images = imageService.getImages(tableTypesId, usersId, postsId);
        return ResponseEntity.ok(images);
    }
}
