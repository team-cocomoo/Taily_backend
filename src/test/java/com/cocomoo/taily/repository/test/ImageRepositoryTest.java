package com.cocomoo.taily.repository.test;

import com.cocomoo.taily.entity.Image;
import com.cocomoo.taily.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class ImageRepositoryTest {
    @Autowired
    private ImageRepository imageRepository;

    @Test
    void findFirstByPostsIdAndTableTypesId(){
        //given
        Image image = Image.builder()
                .uuid("470fd844-ae00-11f0-a30f-c4bde51332be")
                .postsId(1L)
                .tableTypesId(7L)
                .filePath("/uploads/event/470fd862-ae00-11f0-a30f-c4bde51332be_강아지포스터.jpg")
                .fileSize("3419374")
                .createdAt(LocalDateTime.of(2025, 10, 21, 7, 1, 6))
                .updatedAt(LocalDateTime.of(2025, 10, 21, 7, 1, 6))
                .build();
        imageRepository.save(image);
        //when
        Optional<Image> result = imageRepository.findFirstByPostsIdAndTableTypesId(1L, 7L);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getFilePath()).isEqualTo("/uploads/event/470fd862-ae00-11f0-a30f-c4bde51332be_강아지포스터.jpg");
    }

}
