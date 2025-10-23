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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Slf4j
public class WalkPathRepositoryTest {
    @Autowired
    WalkPathRepository walkPathRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void findAllWithUserTest(){
        //Given - 유저 와 페이징 정보
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        //When - DB 에서 실제 조회
        Page<WalkPath> walkPathPage = walkPathRepository.findByUserId(userId, pageable);
        List<WalkPath> walkPaths = walkPathPage.getContent();

        //Then
        log.info("총 게시글 수 (전체): {}", walkPathPage.getTotalElements());
        log.info("현재 페이지 게시글 수: {}", walkPaths.size());

        assertThat(walkPaths).isNotNull();

        if (!walkPaths.isEmpty()) {
            WalkPath first = walkPaths.get(0);
            log.info("첫 번째 게시글 제목: {}", first.getTitle());
            log.info("작성자 ID: {}", first.getUser().getId());
            log.info("작성자 닉네임: {}", first.getUser().getNickname());

            // 모든 게시글이 userId=1인 사용자의 글인지 검증
            walkPaths.forEach(wp ->
                    assertThat(wp.getUser().getId()).isEqualTo(userId)
            );
        } else {
            log.warn("⚠️ user_id={} 에 대한 게시글이 존재하지 않습니다.", userId);
        }
    }

//    @Test
//    void testFindAllWithUser(){
//        //Given : Users
//        User user = userRepository.save(User.builder()
//                .id(1L)
//                .publicId(UUID.randomUUID().toString())
//                .username("tester1")
//                .nickname("테스터")
//                .password("pw1234")
//                .tel("010-1111-1111")
//                .email("tester1@example.com")
//                .address("서울시")
//                .role(UserRole.ROLE_USER)
//                .state(UserState.ACTIVE)
//                .build()
//        );
//        //Given : walkPathRepository
//        walkPathRepository.save(WalkPath.builder()
//                .title("공원 산책")
//                .content("공원에서 밤공기 맡으며 산책해요")
//                .view(12L)
//                .likeCount(0L)
//                .user(user)
//                .build());
//        //When 다 조회
//        List<WalkPath> posts =  walkPathRepository.findAllWithUser();
//        //Then
//        assertThat(posts).hasSize(1);
//        assertThat(posts.get(0).getUser().getUsername()).isEqualTo("tester");
//
//        log.info("조회된 게시물: {}", posts.get(0));
//
//    }
}
