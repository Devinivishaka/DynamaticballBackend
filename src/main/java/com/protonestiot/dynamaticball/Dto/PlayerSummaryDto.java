package com.protonestiot.dynamaticball.Dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryDto {
    private String playerId;
    private double maxSpeed;
    private String penaltyTime;
    private String ballPossessingTime;
    private int ballControlInitiations;   // number of times player initiated ball control
}

