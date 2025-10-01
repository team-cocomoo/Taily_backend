package com.cocomoo.taily.repository.test;

import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.repository.WalkPathRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class WalkPathRepositoryTest {
    @Autowired
    WalkPathRepository walkPathRepository;

    @Test
    void testFindAllWithUser(){
        //Given : 다른 정보들 입력
        walkPathRepository.save(WalkPath.builder()
                .title("공원 산책")
                .content("공원에서 밤공기 맡으며 산책해요")
                .view(12L)
                .build());
        //When 다 조회
        List<WalkPath> posts =  walkPathRepository.findAllWithUser();
        //Then
        assertThat(posts).hasSize(1);

    }
}
