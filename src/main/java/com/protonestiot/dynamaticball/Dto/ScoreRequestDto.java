package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequestDto {
    private String matchId;
    private String teamId;
    private int score; // points to add
    @JsonAlias("gameTime")
    private String timestamp;
    private String playerId;
}
