package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequestDto {
    private String matchId;
    private String teamId;
    private int score; // points to add
    private String timestamp;
    private String playerId;
}
