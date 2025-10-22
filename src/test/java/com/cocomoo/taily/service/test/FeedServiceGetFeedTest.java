package com.cocomoo.taily.service.test;

import com.cocomoo.taily.dto.petstory.FeedRequestDto;
import com.cocomoo.taily.dto.petstory.FeedResponseDto;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.security.config.SecurityConfig;
import com.cocomoo.taily.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD 기반 테스트
 * 피드와 이미지를 저장하고 피드와 이미지 URL 정보 가져오기
 */

@Slf4j
@SpringBootTest
@Import(SecurityConfig.class)
@Transactional

public class FeedServiceGetFeedTest {

    @Autowired
    private FeedService feedService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private Long userId;

    /**
     * 각 테스트 메서드 실행 전에 반드시 한 번씩 실행된다.
     * 테스트 환경을 초기화 하거나 공통 데이터를 세팅할 때 사용한다.
     * 테스트에 필요한 데이터를 준비하는 단계
     */
    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        String uuid = UUID.randomUUID().toString();

        User user = User.builder()
                .publicId(uuid)
                .username("test01")
                .nickname("test01닉네임")
                .password(passwordEncoder.encode("1234"))
                .tel("010-9999-0001")
                .email("test01@gmail.com")
                .address("test01의 집 주소는 어딜까?")
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("테스트용 사용자 생성 완료 = " + user.getId() + user.getUsername());

        userId = user.getId();
    }

    /**
     * 피드 등록 후 피드와 이미지 조회 BDD 테스트
     * @throws IOException
     */
    @Test
    void registerFeedandImageUploadAndGetImage() throws IOException {
        /** given : Mock 이미지 2개 준비
        테스트에 필요한 데이터를 준비하는 단계
        **/
        MockMultipartFile img1 = new MockMultipartFile(
                "images",
                "dog1.jpg",
                "image/jpeg",
                new FileInputStream("src/test/resources/static/dog1.jpg")
        );
        MockMultipartFile img2 = new MockMultipartFile(
                "images",
                "dog2.jpg",
                "image/jpeg",
                new FileInputStream("src/test/resources/static/dog2.jpg")
        );

        FeedRequestDto dto = FeedRequestDto.builder()
                .content("BDD 기반 피드 업로드 테스트")
                .images(List.of(img1, img2))
                .tags(List.of("dog", "smile"))
                .tableTypeId(3L)
                .build();

        // When : 피드 등록 및 DB 저장
        FeedResponseDto savedFeed = feedService.registerFeed(userId, dto);

        // Then : 등록된 피드가 존재하는지 확인
        assertThat(savedFeed).isNotNull();
        assertThat(savedFeed.getId()).isNotNull();
        assertThat(savedFeed.getContent()).isEqualTo("BDD 기반 피드 업로드 테스트");

        log.info("피드 등록 완료: feedId={}, content={}", savedFeed.getId(), savedFeed.getContent());

        // When : 저장된 피드를 다시 조회
        FeedResponseDto fetchedFeed = feedService.getFeed(savedFeed.getId());

        // Then : 이미지 및 태그 확인
        assertThat(fetchedFeed).isNotNull();
        assertThat(fetchedFeed.getImages()).hasSize(2);
        assertThat(fetchedFeed.getImages().get(0)).contains("/uploads/feed/");
        assertThat(fetchedFeed.getImages().get(1)).contains("/uploads/feed/");
        assertThat(fetchedFeed.getTags()).containsExactlyInAnyOrder("dog", "smile");

        // 로그 출력
        log.info("조회된 피드 ID: {}", fetchedFeed.getId());
        log.info("조회된 이미지 경로: {}", fetchedFeed.getImages());
        log.info("조회된 태그: {}", fetchedFeed.getTags());
    }
}