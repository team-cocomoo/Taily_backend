package com.cocomoo.taily.repository.test;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.repository.WalkPathRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class WalkPathRepositoryTest {
    @Autowired
    WalkPathRepository walkPathRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void testFindAllWithUser(){
        //Given : Users
        User user = userRepository.save(User.builder()
                .id(1L)
                .publicId(UUID.randomUUID().toString())
                .username("tester1")
                .nickname("테스터")
                .password("pw1234")
                .tel("010-1111-1111")
                .email("tester1@example.com")
                .address("서울시")
                .role(UserRole.ROLE_USER)
                .state(UserState.ACTIVE)
                .build()
        );
        //Given : walkPathRepository
        walkPathRepository.save(WalkPath.builder()
                .title("공원 산책")
                .content("공원에서 밤공기 맡으며 산책해요")
                .view(12L)
                .likeCount(0L)
                .user(user)
                .build());
        //When 다 조회
        List<WalkPath> posts =  walkPathRepository.findAllWithUser();
        //Then
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getUser().getUsername()).isEqualTo("tester");

        log.info("조회된 게시물: {}", posts.get(0));

    }
}
