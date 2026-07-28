package com.protonestiot.dynamaticball.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatDto {
    private String playerId;
    private Long playerRecordId;
    private Double maxSpeed;
    private String penaltyTime;
    private String ballPossessingTime;
    private Integer ballControlInitiations;
}
