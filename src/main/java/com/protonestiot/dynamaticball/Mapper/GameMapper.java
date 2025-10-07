package com.protonestiot.dynamaticball.Mapper;


import com.protonestiot.dynamaticball.Dto.*;
import com.protonestiot.dynamaticball.Entity.Game;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public Game toEntity(GameRequestDto dto) {
        if (dto == null) return null;
        return Game.builder()
                .teamAName(dto.getTeamAName())
                .teamBName(dto.getTeamBName())
                .teamAColor(dto.getTeamAColor())
                .teamBColor(dto.getTeamBColor())
                .gameTime(dto.getGameTime())
                .playersPerTeam(dto.getPlayersPerTeam())
                .maxHoldTime(dto.getMaxHoldTime())
                .penaltyTime(dto.getPenaltyTime())
                .selectedBall(dto.getSelectedBall())
                .goal1(dto.getGoal1())
                .goal2(dto.getGoal2())
                .build();
    }

    public GameResponseDto toDto(Game entity) {
        if (entity == null) return null;
        return GameResponseDto.builder()
                .id(entity.getId())
                .teamAName(entity.getTeamAName())
                .teamBName(entity.getTeamBName())
                .teamAColor(entity.getTeamAColor())
                .teamBColor(entity.getTeamBColor())
                .gameTime(entity.getGameTime())
                .playersPerTeam(entity.getPlayersPerTeam())
                .maxHoldTime(entity.getMaxHoldTime())
                .penaltyTime(entity.getPenaltyTime())
                .selectedBall(entity.getSelectedBall())
                .goal1(entity.getGoal1())
                .goal2(entity.getGoal2())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .build();
    }
}
