package com.cocomoo.taily.dto.cs;

import com.cocomoo.taily.entity.Faq;
import com.cocomoo.taily.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FaqDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String username;

    public static FaqDetailResponseDto from(Faq faq) {
        User user = faq.getUser();

        return FaqDetailResponseDto.builder()
                .id(faq.getId())
                .title(faq.getTitle())
                .content(faq.getContent())
                .createdAt(faq.getCreatedAt())
                .username(user.getUsername())
                .build();
    }
}
