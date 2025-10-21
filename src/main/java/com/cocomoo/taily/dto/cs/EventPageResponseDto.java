package com.cocomoo.taily.dto.cs;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EventPageResponseDto {
    private List<EventListResponseDto> eventList;
    private long totalCount;
}
