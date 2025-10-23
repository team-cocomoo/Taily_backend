package com.cocomoo.taily.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserPageResponseDto {
    private List<UserListResponseDto> data;
    private long totalCount;
}
