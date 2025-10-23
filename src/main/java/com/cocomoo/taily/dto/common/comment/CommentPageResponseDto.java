package com.cocomoo.taily.dto.common.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageResponseDto {
    private List<CommentResponseDto> content;
    private int page;
    private int totalPages;
    private long totalElements;
}
