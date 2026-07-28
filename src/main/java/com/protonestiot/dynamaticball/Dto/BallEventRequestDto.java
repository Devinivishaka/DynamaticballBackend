package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BallEventRequestDto {
    private String matchId;
    private String eventType;
    private String playerId;
    private String gameTime;
    private String timestamp;
}
