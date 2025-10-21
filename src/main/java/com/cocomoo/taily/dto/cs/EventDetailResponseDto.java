package com.cocomoo.taily.dto.cs;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long view;
    private String username;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
}
