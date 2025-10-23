package com.cocomoo.taily.dto.walkPaths;

import com.cocomoo.taily.entity.WalkPathRoute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkPathRouteResponseDto {
    private Long id;
    private String address;
    private Integer orderNo;

    public static WalkPathRouteResponseDto from(WalkPathRoute route) {
        return WalkPathRouteResponseDto.builder()
                .id(route.getId())
                .address(route.getAddress())
                .orderNo(route.getOrderNo())
                .build();
    }
}
