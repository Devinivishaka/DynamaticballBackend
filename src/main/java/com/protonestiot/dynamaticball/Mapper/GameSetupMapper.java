package com.protonestiot.dynamaticball.Mapper;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Entity.GameSetup;
import com.protonestiot.dynamaticball.Entity.Team;
import com.protonestiot.dynamaticball.Entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

public class GameSetupMapper {

    public static GameSetup toEntity(GameSetupRequestDto dto) {
        String code = "GS_" + UUID.randomUUID().toString().substring(0, 8);
        GameSetup gs = GameSetup.builder()
                .setupCode(code)
                .gameTime(dto.getGameSettings().getGameTime())
                .playersPerTeam(dto.getGameSettings().getPlayersPerTeam())
                .maxHoldTime(dto.getGameSettings().getMaxHoldTime())
                .penaltyTime(dto.getGameSettings().getPenaltyTime())
                .selectedBall(dto.getConnectBall() != null ? dto.getConnectBall().getSelectedBall() : null)
                .goal1(dto.getConnectGoals() != null ? dto.getConnectGoals().getGoal1() : null)
                .goal2(dto.getConnectGoals() != null ? dto.getConnectGoals().getGoal2() : null)
                .build();

        if (dto.getTeams() != null) {
            if (dto.getTeams().getTeamA() != null) {
                Team teamA = Team.builder()
                        .teamKey("teamA")
                        .name(dto.getTeams().getTeamA().getName())
                        .color(dto.getTeams().getTeamA().getColor())
                        .goal(dto.getTeams().getTeamA().getGoal())
                        .gameSetup(gs)
                        .build();
                if (dto.getTeams().getTeamA().getPlayers() != null) {
                    teamA.setPlayers(dto.getTeams().getTeamA().getPlayers().stream().map(pdto ->
                            Player.builder()
                                    .playerCode(pdto.getPlayerId())
                                    .belt(pdto.getBelt())
                                    .rightWristband(pdto.getRightWristband())
                                    .leftWristband(pdto.getLeftWristband())
                                    .camera(pdto.getCamera())
                                    .team(teamA)
                                    .build()
                    ).collect(Collectors.toList()));
                }
                gs.getTeams().add(teamA);
            }
            if (dto.getTeams().getTeamB() != null) {
                Team teamB = Team.builder()
                        .teamKey("teamB")
                        .name(dto.getTeams().getTeamB().getName())
                        .color(dto.getTeams().getTeamB().getColor())
                        .goal(dto.getTeams().getTeamB().getGoal())
                        .gameSetup(gs)
                        .build();
                if (dto.getTeams().getTeamB().getPlayers() != null) {
                    teamB.setPlayers(dto.getTeams().getTeamB().getPlayers().stream().map(pdto ->
                            Player.builder()
                                    .playerCode(pdto.getPlayerId())
                                    .belt(pdto.getBelt())
                                    .rightWristband(pdto.getRightWristband())
                                    .leftWristband(pdto.getLeftWristband())
                                    .camera(pdto.getCamera())
                                    .team(teamB)
                                    .build()
                    ).collect(Collectors.toList()));
                }
                gs.getTeams().add(teamB);
            }
        }
        return gs;
    }

    public static void updateEntity(GameSetup existing, GameSetupRequestDto dto) {

        // --- Update top-level fields ---
        existing.setGameTime(dto.getGameSettings().getGameTime());
        existing.setPlayersPerTeam(dto.getGameSettings().getPlayersPerTeam());
        existing.setMaxHoldTime(dto.getGameSettings().getMaxHoldTime());
        existing.setPenaltyTime(dto.getGameSettings().getPenaltyTime());
        existing.setSelectedBall(dto.getConnectBall().getSelectedBall());
        existing.setGoal1(dto.getConnectGoals().getGoal1());
        existing.setGoal2(dto.getConnectGoals().getGoal2());

        // --- Update Teams ---
        if (existing.getTeams() != null && existing.getTeams().size() >= 2) {
            Team teamA = existing.getTeams().get(0);
            Team teamB = existing.getTeams().get(1);

            // 🦁 Update Team A
            if (dto.getTeams().getTeamA() != null) {
                teamA.setName(dto.getTeams().getTeamA().getName());
                teamA.setColor(dto.getTeams().getTeamA().getColor());
                teamA.setGoal(dto.getTeams().getTeamA().getGoal());

                teamA.getPlayers().clear();
                if (dto.getTeams().getTeamA().getPlayers() != null) {
                    teamA.getPlayers().addAll(dto.getTeams().getTeamA().getPlayers().stream()
                            .map(pdto -> Player.builder()
                                    .playerCode(pdto.getPlayerId())
                                    .belt(pdto.getBelt())
                                    .rightWristband(pdto.getRightWristband())
                                    .leftWristband(pdto.getLeftWristband())
                                    .camera(pdto.getCamera())
                                    .team(teamA)
                                    .build())
                            .collect(Collectors.toList()));
                }
            }

            // 🐯 Update Team B
            if (dto.getTeams().getTeamB() != null) {
                teamB.setName(dto.getTeams().getTeamB().getName());
                teamB.setColor(dto.getTeams().getTeamB().getColor());
                teamB.setGoal(dto.getTeams().getTeamB().getGoal());

                teamB.getPlayers().clear();
                if (dto.getTeams().getTeamB().getPlayers() != null) {
                    teamB.getPlayers().addAll(dto.getTeams().getTeamB().getPlayers().stream()
                            .map(pdto -> Player.builder()
                                    .playerCode(pdto.getPlayerId())
                                    .belt(pdto.getBelt())
                                    .rightWristband(pdto.getRightWristband())
                                    .leftWristband(pdto.getLeftWristband())
                                    .camera(pdto.getCamera())
                                    .team(teamB)
                                    .build())
                            .collect(Collectors.toList()));
                }
            }
        }
    }

}
