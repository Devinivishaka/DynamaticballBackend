package com.protonestiot.dynamaticball.Service.Impl;

import com.protonestiot.dynamaticball.Dto.GameSetupRequestDto;
import com.protonestiot.dynamaticball.Dto.GameSetupResponseDto;
import com.protonestiot.dynamaticball.Entity.GameSetup;
import com.protonestiot.dynamaticball.Exception.GameSetupException;
import com.protonestiot.dynamaticball.Mapper.GameSetupMapper;
import com.protonestiot.dynamaticball.Repository.GameSetupRepository;
import com.protonestiot.dynamaticball.Service.GameSetupService;
import com.protonestiot.dynamaticball.Entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameSetupServiceImpl implements GameSetupService {

    private final GameSetupRepository gameSetupRepository;

    @Override
    @Transactional
    public GameSetupResponseDto saveGameSetup(GameSetupRequestDto requestDto) {

        if (requestDto == null)
            throw new GameSetupException("Game setup request cannot be null.");

        if (requestDto.getGameSettings() == null)
            throw new GameSetupException("Game settings are required before connecting the ball.");

        if (requestDto.getGameSettings().getGameTime() <= 0)
            throw new GameSetupException("Game time must be greater than 0 minutes.");

        if (requestDto.getGameSettings().getPlayersPerTeam() <= 0)
            throw new GameSetupException("Players per team must be 3 or 5.");

        if (requestDto.getGameSettings().getMaxHoldTime() <= 0)
            throw new GameSetupException("Maximum hold time must be greater than 0 seconds.");

        if (requestDto.getGameSettings().getPenaltyTime() < 0)
            throw new GameSetupException("Penalty time must be 0 or greater.");


        if (requestDto.getConnectBall() == null ||
                isEmpty(requestDto.getConnectBall().getSelectedBall()))
            throw new GameSetupException("A ball must be selected before continuing.");


        if (requestDto.getConnectGoals() == null
                || isEmpty(requestDto.getConnectGoals().getGoal1())
                || isEmpty(requestDto.getConnectGoals().getGoal2()))
            throw new GameSetupException("Both goals must be connected (goal1 and goal2) before adding teams.");


        if (requestDto.getTeams() == null
                || requestDto.getTeams().getTeamA() == null
                || requestDto.getTeams().getTeamB() == null)
            throw new GameSetupException("Both Team A and Team B must be provided.");

        if (isEmpty(requestDto.getTeams().getTeamA().getName()))
            throw new GameSetupException("Team A name must be provided.");
        if (isEmpty(requestDto.getTeams().getTeamB().getName()))
            throw new GameSetupException("Team B name must be provided.");

        if (isEmpty(requestDto.getTeams().getTeamA().getColor()))
            throw new GameSetupException("Team A color must be provided.");
        if (isEmpty(requestDto.getTeams().getTeamB().getColor()))
            throw new GameSetupException("Team B color must be provided.");

        if (isEmpty(requestDto.getTeams().getTeamA().getGoal()))
            throw new GameSetupException("Team A goal must be assigned.");
        if (isEmpty(requestDto.getTeams().getTeamB().getGoal()))
            throw new GameSetupException("Team B goal must be assigned.");


        GameSetup entity = GameSetupMapper.toEntity(requestDto);

        long count = gameSetupRepository.countBySetupCodeIsNotNull() + 1;
        String setupCode = String.format("GS_%03d", count);
        entity.setSetupCode(setupCode);

        GameSetup saved = gameSetupRepository.save(entity);

        Team teamA = saved.getTeams().stream().filter(t -> "teamA".equals(t.getTeamKey())).findFirst().orElse(null);
        Team teamB = saved.getTeams().stream().filter(t -> "teamB".equals(t.getTeamKey())).findFirst().orElse(null);

        Long teamAId = teamA != null ? teamA.getId() : null;
        Long teamBId = teamB != null ? teamB.getId() : null;

        return GameSetupResponseDto.builder()
                .success(true)
                .gameSetupId(saved.getSetupCode())
                .teamAId(teamAId)
                .teamBId(teamBId)
                .teams(getTeamsResponse(teamA, teamB))
                .message("Game setup saved successfully.")
                .build();
    }

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }

    private GameSetupResponseDto.TeamsResponse getTeamsResponse(Team teamA, Team teamB) {
        if (teamA == null && teamB == null) return null;

        GameSetupResponseDto.TeamResponse teamAResponse = null;
        if (teamA != null) {
            teamAResponse = GameSetupResponseDto.TeamResponse.builder()
                    .teamId(teamA.getId())
                    .players(teamA.getPlayers().stream()
                            .map(p -> GameSetupResponseDto.PlayerResponse.builder()
                                    .playerRecordId(p.getId())
                                    .playerId(p.getPlayerCode())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

        GameSetupResponseDto.TeamResponse teamBResponse = null;
        if (teamB != null) {
            teamBResponse = GameSetupResponseDto.TeamResponse.builder()
                    .teamId(teamB.getId())
                    .players(teamB.getPlayers().stream()
                            .map(p -> GameSetupResponseDto.PlayerResponse.builder()
                                    .playerRecordId(p.getId())
                                    .playerId(p.getPlayerCode())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

        return GameSetupResponseDto.TeamsResponse.builder()
                .teamA(teamAResponse)
                .teamB(teamBResponse)
                .build();
    }


    @Override
    @Transactional
    public GameSetupResponseDto updateGameSetup(String gameSetupId, GameSetupRequestDto requestDto) {

        if (requestDto == null)
            throw new GameSetupException("Game setup update request cannot be null.");

        GameSetup existing = gameSetupRepository.findBySetupCode(gameSetupId)
                .orElseThrow(() -> new GameSetupException("Game setup not found for ID: " + gameSetupId));

        if (requestDto.getGameSettings() == null)
            throw new GameSetupException("Game settings are required.");
        if (requestDto.getGameSettings().getGameTime() <= 0)
            throw new GameSetupException("Game time must be greater than 0.");
        if (requestDto.getGameSettings().getPlayersPerTeam() <= 0)
            throw new GameSetupException("Players per team must be greater than 0.");
        if (requestDto.getConnectBall() == null || isEmpty(requestDto.getConnectBall().getSelectedBall()))
            throw new GameSetupException("Ball selection is required.");
        if (requestDto.getConnectGoals() == null
                || isEmpty(requestDto.getConnectGoals().getGoal1())
                || isEmpty(requestDto.getConnectGoals().getGoal2()))
            throw new GameSetupException("Both goals (goal1 and goal2) must be connected.");
        if (requestDto.getTeams() == null
                || requestDto.getTeams().getTeamA() == null
                || requestDto.getTeams().getTeamB() == null)
            throw new GameSetupException("Both teams (A and B) must be provided.");

        GameSetupMapper.updateEntity(existing, requestDto);

        GameSetup saved = gameSetupRepository.save(existing);

        Team teamA = saved.getTeams().stream().filter(t -> "teamA".equals(t.getTeamKey())).findFirst().orElse(null);
        Team teamB = saved.getTeams().stream().filter(t -> "teamB".equals(t.getTeamKey())).findFirst().orElse(null);

        Long teamAId = teamA != null ? teamA.getId() : null;
        Long teamBId = teamB != null ? teamB.getId() : null;

        return GameSetupResponseDto.builder()
                .success(true)
                .gameSetupId(saved.getSetupCode())
                .message("Game setup updated successfully")
                .teamAId(teamAId)
                .teamBId(teamBId)
                .teams(getTeamsResponse(teamA, teamB))
                .build();
    }

}
