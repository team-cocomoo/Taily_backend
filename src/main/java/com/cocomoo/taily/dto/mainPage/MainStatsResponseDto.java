package com.cocomoo.taily.dto.mainPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainStatsResponseDto {

    private Long petCount;
    private Long feedCount;
    private Long walkCount;
    private Long userCount;
}
