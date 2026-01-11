package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStatsUpsertRequestDto {

    private String matchId;
    private String timestamp;

    // key = playerId
    private Map<String, PlayerStatsItemDto> playerStats;

    // key = teamA / teamB
    private Map<String, TeamStatsItemDto> teamStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlayerStatsItemDto {
        private String playerId;
        private Double maxSpeedKmh;
        private Integer totalPenaltySeconds;
        private Integer totalPossessionSeconds;
        private Integer ballControlInitiations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamStatsItemDto {
        private String teamId;
        private Integer totalPossessionSeconds;
        private Integer totalPenaltySeconds;
        private Integer totalBallControlInitiations;
    }
}



