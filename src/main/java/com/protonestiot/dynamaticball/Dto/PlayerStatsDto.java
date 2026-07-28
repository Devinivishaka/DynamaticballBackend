package com.protonestiot.dynamaticball.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlayerStatsDto {
    private Long playerRecordId;
    private String playerId;
    private Double maxSpeed;
    private String penaltyTime;
    private String ballPossessingTime;
    private Integer ballControlInitiations;
}
