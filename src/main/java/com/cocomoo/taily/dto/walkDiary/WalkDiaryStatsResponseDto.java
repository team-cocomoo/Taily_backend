package com.cocomoo.taily.dto.walkDiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalkDiaryStatsResponseDto {
    private int totalWalks;
    private double avgDurationMinutes;
    private long streakDays;
    private List<DailyStat> dailyStat;
    private List<HourlyStat> hourlyStat;
    private String reminderMessage;

    @Getter
    @AllArgsConstructor
    public static class DailyStat {
        private LocalDate date;
        private Long durationMinutes;
    }
    @Getter
    @AllArgsConstructor
    public static class HourlyStat {
        private int hour;
        private Long durationMinutes;
    }

    public static WalkDiaryStatsResponseDto empty() {
        return WalkDiaryStatsResponseDto.builder()
                .totalWalks(0)
                .avgDurationMinutes(0)
                .streakDays(0)
                .dailyStat(List.of())
                .hourlyStat(List.of())
                .reminderMessage("이번 달에는 아직 산책 기록이 없어요 🐾")
                .build();
    }
}
