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
    private String penaltyTime;           // e.g., "2:30"
    private String ballPossessingTime;    // e.g., "8:45"
    private int ballControlInitiations;   // number of times player initiated ball control
}

