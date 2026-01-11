package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStatsResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        private String matchId;
        private String timestamp;
        private Map<String, MatchStatsUpsertRequestDto.PlayerStatsItemDto> playerStats;
        private Map<String, MatchStatsUpsertRequestDto.TeamStatsItemDto> teamStats;
    }
}

