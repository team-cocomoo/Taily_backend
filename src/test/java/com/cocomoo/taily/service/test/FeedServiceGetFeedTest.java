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
 * 피드와 이미지를 저장하고 이미지 URL 가져오기 테스트
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
        log.info("저장된 userId = " + user.getId());
        log.info("저장된 username = " + user.getUsername());

        userId = user.getId();
    }

    /**
     * 피드 등록 후 피드와 이미지 조회
     * BDD
     * Given : 사용자가 존재하고
     * And : 이미지 2개가 준비되어 있을 때
     * When : 사용자가 피드를 업로드 하면
     * Then : 피드 내용이 저장되고
     * And : 이미지 2개의 URL이 반환된다.
     * And : 태그도 저장된다.
     * @throws IOException
     */
    @Test
    void registerFeedandImageUploadAndGetImage() throws IOException {
        /** given : Mock 이미지 2개 생성
        테스트에 필요한 데이터를 준비하는 단계
        MockMultipartFile : 테스트용으로 가짜 이미지 파일 생성
        이미지 업로드 로직 테스트에 필요
        FeedRequestDto : Feed 등록에 필요한 정보 객체
        피드 내용(content), 이미지(images), 태그(tags), 테이블 타입(tableTypeId) 포함
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
                .content("테스트 피드 내용입니다.")
                .images(List.of(img1, img2))
                .tags(List.of("dog", "cute"))
                .tableTypeId(3L)
                .build();

        /** when : 실제 메서드 호출
         registerFeed(userId, dto)
         사용자 ID와 DTO를 기반으로 피드 등록
         이미지 업로드 및 태그 처리까지 수행
         최종적으로 FeedResponseDto 반환
        */
        FeedResponseDto registedFeed = feedService.registerFeed(userId, dto);

        // And: getFeed() 호출하여 피드와 연결된 모든 이미지 가져오기
        FeedResponseDto fetchedFeed = feedService.getFeed(registedFeed.getId());

        // Then : 피드 정보와 이미지 확인

        // 로그 출력

    }
}
