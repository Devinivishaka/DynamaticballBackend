package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlayerStatsDto {
    private String playerId;
    private String maxSpeed;             // string as per response
    private String penaltyTime;
    private String ballPossessingTime;
    private String ballControlInitiations;
}
