package com.cocomoo.taily.dto.myPage;

import com.cocomoo.taily.dto.cs.FaqDetailResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyLikesPageResponseDto {
    private List<MyLikesResponseDto> myLikeList;
    private int page;   // 현재 페이지 번호
    private int size;   // 페이지당 사이즈
    private long totalCount;    // 전체 좋아요 수
    private int totalPages;
    private boolean isLast; // 마지막 페이지 여부
}
