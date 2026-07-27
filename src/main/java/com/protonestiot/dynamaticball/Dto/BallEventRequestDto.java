package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BallEventRequestDto {
    private String matchId;
    private String eventType;
    private String playerId;
    @JsonAlias("gameTime")
    private String timestamp;
}
