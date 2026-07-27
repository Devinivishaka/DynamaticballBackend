package com.protonestiot.dynamaticball.Dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionRequestDto {
    private String matchId; // matchCode
    @JsonAlias("gameTime")
    private String timestamp;
}
