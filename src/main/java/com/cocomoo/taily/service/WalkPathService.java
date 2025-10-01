package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.WalkPathListResponseDto;
import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.repository.WalkPathRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
@Slf4j
public class WalkPathService {
    private final WalkPathRepository walkPathRepository;

    public List<WalkPathListResponseDto> findAllPostList() {

        List<WalkPath> posts = walkPathRepository.findAllWithUser(); // JPQL Fetch Join 적용된 repository 메서드 호출
        return posts.stream().map(WalkPathListResponseDto::from).collect(Collectors.toUnmodifiableList());
    }



}
