package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSetupRequestDto {

    private GameSettings gameSettings;
    private ConnectBall connectBall;
    private ConnectGoals connectGoals;
    private Teams teams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSettings {
        private int gameTime;
        private int playersPerTeam;
        private int maxHoldTime;
        private int penaltyTime;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ConnectBall {
        private String selectedBall;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ConnectGoals {
        private String goal1;
        private String goal2;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Teams {
        private TeamDto teamA;
        private TeamDto teamB;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TeamDto {
        private String name;
        private String color;
        private String goal;
        private List<PlayerDto> players;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PlayerDto {
        private String playerId;
        private String belt;
        private String rightWristband;
        private String leftWristband;
        private String camera;
    }
}
