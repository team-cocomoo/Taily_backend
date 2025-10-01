package com.cocomoo.taily.controller;

import com.cocomoo.taily.dto.WalkPathListResponseDto;
import com.cocomoo.taily.service.WalkPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/walkpaths")
@RequiredArgsConstructor
@Slf4j
public class WalkPathController {
    private final WalkPathService walkPathService;

    /**
     * walkPath 전체 게시물 조회
     * @return
     */
    @GetMapping
    public ResponseEntity<List<WalkPathListResponseDto>> finaAllWalkPathList(){
        List<WalkPathListResponseDto> walkPaths = walkPathService.findAllPostList();
        return ResponseEntity.ok(walkPaths);
    }

}
