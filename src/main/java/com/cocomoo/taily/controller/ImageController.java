package com.cocomoo.taily.controller;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.security.user.CustomUserDetails;
import com.cocomoo.taily.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 업로드
     * - subFolder가 null이거나 빈 경우, tableTypesId 기반으로 자동 설정
     * - table_types 테이블 기준으로 폴더명 지정
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "subFolder", required = false) String subFolder,
            @RequestParam("tableTypesId") Long tableTypesId,
            @RequestParam(value = "postsId", required = false) Long postsId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        Long usersId = (userDetails != null) ? userDetails.getUserId() : null;

        // subFolder 자동 지정
        if (subFolder == null || subFolder.isBlank()) {
            subFolder = switch (tableTypesId.intValue()) {
                case 1 -> "user";          // USERS
                case 2 -> "pet";           // PETS
                case 3 -> "feed";          // FEEDS
                case 4 -> "walk_diary";   // WALK_DIARIES
                case 5 -> "taily_friend";  // TAILY_FRIENDS
                case 6 -> "walk_path";     // WALK_PATHS
                case 7 -> "event";         // EVENTS
                default -> "etc";
            };
            log.info("subFolder 자동 지정됨 (tableTypesId={} → {}):", tableTypesId, subFolder);
        }

        log.info("이미지 업로드 요청: subFolder={}, tableTypesId={}, usersId={}, postsId={}",
                subFolder, tableTypesId, usersId, postsId);

        List<Image> images = imageService.uploadImages(subFolder, tableTypesId, usersId, postsId, files);
        return ResponseEntity.ok(images);
    }

    /**
     * 이미지 조회
     */
    @GetMapping
    public ResponseEntity<List<Image>> getImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("tableTypesId") Long tableTypesId,
            @RequestParam(value = "usersId", required = false) Long usersId,
            @RequestParam(value = "postsId", required = false) Long postsId
    ) {
        // 로그인 사용자 자동 세팅
        if (tableTypesId == 1L && usersId == null && userDetails != null) {
            usersId = userDetails.getUserId();
        }

        log.info("이미지 조회 요청: tableTypesId={}, usersId={}, postsId={}", tableTypesId, usersId, postsId);
        List<Image> images = imageService.getImages(tableTypesId, usersId, postsId);
        return ResponseEntity.ok(images);
    }

    /**
     * 이미지 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImages(
            @RequestParam("tableTypesId") Long tableTypesId,
            @RequestParam(value = "usersId", required = false) Long usersId,
            @RequestParam(value = "postsId", required = false) Long postsId
    ) {
        log.info("이미지 삭제 요청: tableTypesId={}, usersId={}, postsId={}", tableTypesId, usersId, postsId);
        imageService.deleteImages(tableTypesId, usersId, postsId);
        return ResponseEntity.ok("이미지 삭제 완료");
    }
}
