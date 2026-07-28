package com.protonestiot.dynamaticball.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionRequestDto {
    private String matchId; // matchCode
    private String gameTime;
    private String timestamp;
    private List<PlayerStatDto> playerStats;
}
