package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class PlayerStatsResponseDto {
    private List<PlayerStatsDto> teamA;
    private List<PlayerStatsDto> teamB;
}
