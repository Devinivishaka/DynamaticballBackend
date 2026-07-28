package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRequestDto {
    private String matchId;
    private String teamId;
    private int score; // points to add
    private String gameTime;
    private String timestamp;
    private String playerId;
}
