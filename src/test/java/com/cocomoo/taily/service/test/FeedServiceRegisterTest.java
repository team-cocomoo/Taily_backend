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
import org.junit.jupiter.api.Test;
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

@Slf4j
@SpringBootTest
@Import(SecurityConfig.class)
@Transactional
class FeedServiceRegisterTest {

    @Autowired
    private FeedService feedService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private Long userId;

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
        log.info("저장된 userId = " + user.getId());
        log.info("저장된 username = " + user.getUsername());

        userId = user.getId();
    }

    @Test
    void 피드작성_및_이미지업로드_테스트() throws IOException {
        // given : Mock 이미지 2개 생성
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
                .content("테스트 피드 내용입니다 🐶")
                .images(List.of(img1, img2))
                .tags(List.of("dog", "cute"))
                .tableTypeId(3L)
                .build();

        // when
        FeedResponseDto response = feedService.registerFeed(userId, dto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("테스트 피드 내용입니다 🐶");
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getTags()).contains("dog", "cute");

        // 로그 출력
        log.info("생성된 피드 ID: {}", response.getId());
        log.info("이미지 경로: {}", response.getImages());
    }
}