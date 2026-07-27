package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSetupResponseDto {
    private boolean success;
    private String gameSetupId;
    private String message;
    private Long teamAId;
    private Long teamBId;

    private TeamsResponse teams;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TeamsResponse {
        private TeamResponse teamA;
        private TeamResponse teamB;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TeamResponse {
        private Long teamId;
        private List<PlayerResponse> players;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlayerResponse {
        private Long playerRecordId;
        private String playerId;
    }
}

