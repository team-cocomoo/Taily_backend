package com.cocomoo.taily.dto.walkPaths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkPathRouteRequestDto {
    private String address;
    private Integer orderNo;
}
