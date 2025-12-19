package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchWsEventDto {

    private String event;
    private String matchCode;
    private String timestamp;

    private MatchData match;
    private TeamData team;
    private PlayerData player;

    @Data
    @Builder
    public static class MatchData {
        private String status;
        private Integer scoreTeamA;
        private Integer scoreTeamB;
    }

    @Data
    @Builder
    public static class TeamData {
        private Long teamId;
        private String teamKey;
        private Integer score;
    }

    @Data
    @Builder
    public static class PlayerData {
        private String playerId;
        private Integer playerScore;
        private Integer penaltySeconds;
    }
}
