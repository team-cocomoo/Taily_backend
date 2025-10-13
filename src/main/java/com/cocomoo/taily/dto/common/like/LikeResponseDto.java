package com.cocomoo.taily.dto.common.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDto {
    private boolean liked;
    private long likeCount;
}