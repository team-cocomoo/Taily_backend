package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.ApiResponseDto;
import com.cocomoo.taily.dto.tailyFriends.TailyFriendDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathDetailResponseDto;
import com.cocomoo.taily.dto.walkPaths.WalkPathListResponseDto;
import com.cocomoo.taily.service.WalkPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/walkpaths")
@RequiredArgsConstructor
@Slf4j
public class WalkPathController {
    private final WalkPathService walkPathService;

    //walkPath 전체 게시물 조회

    @GetMapping
    public ResponseEntity<List<WalkPathListResponseDto>> finaAllWalkPathList(){
        List<WalkPathListResponseDto> walkPaths = walkPathService.findAllPostList();
        return ResponseEntity.ok(walkPaths);
    }

    //walkpath 상세 게시물 조회

    @GetMapping("/{id}")
    public ResponseEntity<?> getWalkPathById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        WalkPathDetailResponseDto post = walkPathService.getWalkPathById(id,username);
        log.info("게시글 조회 성공 : title = {}",post.getTitle());
        return ResponseEntity.ok(ApiResponseDto.success(post,"게시글 조회 성공"));
    }

}
