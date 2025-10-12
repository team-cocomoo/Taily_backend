package com.cocomoo.taily.dto.userprofile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedImageResponseDto {
    private Long feedId;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
