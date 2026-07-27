package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyRequestDto {
    private String matchId;
    private String eventType;
    private String playerId;
    private String penaltyTime;
    @JsonProperty("timestamp")
    @JsonAlias("gameTime")
    private String timestamp;
}
